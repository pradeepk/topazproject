@Graphs({
  @Graph(id = "str", uri = "local:///topazproject#str",
         type = "http://topazproject.org/graphs#StringCompare"),
  @Graph(id = "xsd", uri = "local:///topazproject#xsd",
         type = Rdf.mulgara + "XMLSchemaModel"),
  @Graph(id = "prefix", uri = "local:///topazproject#prefix",
         type = Rdf.mulgara + "PrefixGraph"),
  @Graph(id = "photo", uri = "local:///topazproject#photo"),
  @Graph(id = "foaf",  uri = "local:///topazproject#foaf")
})

@Aliases({
  @Alias(alias = "dc",       value = Rdf.dc),
  @Alias(alias = "topaz",    value = Rdf.topaz),
  @Alias(alias = "foaf",     value = Rdf.foaf)
})

package org.topazproject.examples.photo;

import org.topazproject.otm.annotations.Alias;
import org.topazproject.otm.annotations.Aliases;
import org.topazproject.otm.annotations.Graph;
import org.topazproject.otm.annotations.Graphs;
import org.topazproject.otm.Rdf;
