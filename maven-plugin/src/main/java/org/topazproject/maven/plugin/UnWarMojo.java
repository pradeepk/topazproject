package org.topazproject.maven.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;

import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;


/**
 * Maven plugin to explode a war file into the target build path. 
 * This is useful in situations where you want to create a new web-app 
 * by adding your stuff to it. Created mainly for cocoon where you get a
 * cocoon war file from a maven repository and add your app to it and re-war it.
 * 
 * @goal unwar
 * @description explode dependency war files
 */
public class UnWarMojo extends AbstractMojo {
    /**
     * Parameter for project dependencies. Can not configure this in the pom file.
     * @parameter expression="${project.dependencies}"
     * @readonly
     */
    private ArrayList dependencies;
    
    /**
     * Parameter for local repository. Usually no need to configure in the pom file.
     * @parameter expression="${settings.localRepository}"
     */
    private String localRepository;

    /**
     * Parameter for groupId for the war file dependency that we need to explode. 
     * If no groupId is specified in the pom file, any groupId will match. Typically 
     * in a project you are interested in exploding one war file and adding your stuff.
     * So in those cases, the default is good enough.
     * @parameter alias="war-groupId" expression=""
     */
    private String groupId;

    /**
     * Parameter for artifactId for the war file dependency that we need to explode. 
     * Similar treatment as groupId.
     * @parameter alias="war-artifactId" expression=""
     */
    private String artifactId;

    /**
     * Parameter for version for the war file dependency that we need to explode.
     * Similar treatment as groupId
     * @parameter alias="war-version" expression=""
     */
    private String version;

    /**
     * Parameter for target. Usually no need to configure in the pom file. 
     * Otherwise specify the destination directory for unwar.
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     */
    private String target;
    
    /**
     * The local maven repository.
     */
    private File repository;
    
    
    /**
     * Execute this mojo. 
     * @throws MojoExecutionException on error
     */
    public void execute() throws MojoExecutionException{
        findLocalRepository();
        explodeWars();
    }
    
    /**
     * Go thru dependency list and explode any matching wars.
     */
    private void explodeWars() throws MojoExecutionException {
        
        getLog().debug( "looking at dependencies ..." );

        if ((dependencies == null) || (dependencies.size() == 0)){
            getLog().warn("no dependencies. nothing to do.");
            return;
        }
        
        int count = 0;
        
        Iterator it = dependencies.iterator();
        while (it.hasNext()){
            Dependency deps = (Dependency)it.next();
            
            getLog().debug(deps.toString());
            
            if (isAMatch(deps)){
                explode(deps);      
                count++;
            }
        }

        if (count == 0)
            getLog().warn("no matching war files found.");
        else if (count == 1)
            getLog().debug("done exploding war file");
        else
            getLog().warn("exploded " + count + " war files.");
    }
    
    /**
     * Figure out where the maven local repository is. 
     */ 
    private void findLocalRepository() throws MojoExecutionException {
        getLog().debug("examining repository base ...");
        
        File f = new File(localRepository);
        if (!f.exists() && !f.isDirectory())
            throw new MojoExecutionException("repositoryBase parameter '" + f.getPath() 
                    + "' must be a valid path");
        
        repository = f;

        getLog().debug("found repository " + repository.getPath());
    }
    
    /**
     * do we care about this dependency?
     */ 
    private boolean isAMatch(Dependency deps){
        // Must be war
        if (!"war".equals(deps.getType()))
            return false;
            
        // If a groupId is configured it must match
        if ((groupId != null) && !"".equals(groupId) && !groupId.equals(deps.getGroupId()))
            return false;
            
        // If an artifactId is configured it must match
        if ((artifactId != null) && !"".equals(artifactId) 
                && !artifactId.equals(deps.getArtifactId()))
            return false;
            
        // If a version is configured it must match
        if ((version != null) && !"".equals(version) && !version.equals(deps.getVersion()))
            return false;

        return true;
    }

    /**
     * Resolve the war file and explode to build target.
     */ 
    private void explode(Dependency deps) throws MojoExecutionException{
        getLog().info("exploding " + deps.toString());

        // eg. cocoon/cocoon-war/2.1.7/cocoon-war-2.1.7.war
        String path = deps.getGroupId() + File.separatorChar +
                        deps.getArtifactId() + File.separatorChar +
                        deps.getVersion() + File.separatorChar +
                        deps.getArtifactId() + '-' +
                        deps.getVersion() + '.' + deps.getType();

        File war = new File(repository, path);
        
        if (!war.exists())
            throw new MojoExecutionException("could not find file: " + war.getPath());
        
        explode(war);
    
    }

    /**
     * Explode the war to build target.
     */ 
    private void explode(File war) throws MojoExecutionException {
        getLog().info("     from " + war.getPath());
        
        File to = new File(target);
        getLog().info("       to " + to.getPath());
        
        int BUFFER = 4096; 
        
        try {
            BufferedOutputStream dest = null;
         
            FileInputStream fis = new FileInputStream(war);
            JarInputStream jis = new JarInputStream(new BufferedInputStream(fis));
            
            JarEntry entry;
            int count;
            byte data[] = new byte[BUFFER];
            
            while((entry = jis.getNextJarEntry()) != null) {
                getLog().debug("Extracting: " +entry);
                
                File f = new File(to, entry.getName());
                File d = entry.isDirectory() ? f : f.getParentFile();
                
                if (!d.exists() && !d.mkdirs())
                    throw new IOException("mkdir failed for " + d.getPath());
                        
                if (!entry.isDirectory()){    
                    // write the files to the disk
                    FileOutputStream fos = new FileOutputStream(f);
                    dest = new BufferedOutputStream(fos, BUFFER);
                
           
                    while ((count = jis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                
                    dest.flush();
                    dest.close();
                }
            }
            jis.close();
      
        } catch(Exception t) {
            MojoExecutionException e = new MojoExecutionException("error while unzip");
            e.initCause(t);
            throw e;
        } 
    }
    
}
