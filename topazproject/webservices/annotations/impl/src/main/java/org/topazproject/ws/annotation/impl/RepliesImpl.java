package org.topazproject.ws.annotation.impl;

import java.net.URI;
import java.net.URISyntaxException;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;

import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.ws.annotation.NoSuchIdException;
import org.topazproject.ws.annotation.Replies;
import org.topazproject.ws.annotation.ReplyInfo;
import org.topazproject.ws.annotation.ReplyThread;

/**
 * Implementation of Replies.
 *
 * @author Pradeep Krishnan
 */
public class RepliesImpl implements Replies {
  private static final Log    log          = LogFactory.getLog(RepliesImpl.class);
  private static final Map    aliases      = ItqlHelper.getDefaultAliases();
  private static final String MODEL        = "<rmi://localhost/fedora#ri>";
  private static final String REPLY_PID_NS = "reply";

  //
  private static final String ITQL_CREATE =
    ("insert <${id}> <r:type> <tr:Reply> <${id}> <r:type> <${type}>"
    + " <${id}> <tr:root> <${root}> <${id}> <a:created> '${created}'"
    + " <${id}> <tr:inReplyTo> <${inReplyTo}> <${id}> <a:body> <${body}>"
    + " <${id}> <d:creator> '${user}' into ${MODEL};").replaceAll("\\Q${MODEL}", MODEL);

  // 
  private static final String ITQL_INSERT_TITLE =
    ("insert <${id}> <d:title> '${title}' into ${MODEL};").replaceAll("\\Q${MODEL}", MODEL);

  //
  private static final String ITQL_CHECK_ID =
    ("select $s from ${MODEL} where $s <r:type> <tr:Reply> and $s <tucana:is> <${id}>        ;")
     .replaceAll("\\Q${MODEL}", MODEL);

  // don't use it for (inReplyTo == root)
  private static final String ITQL_CHECK_IN_REPLY_TO =
    ("select $s from ${MODEL} where $s <r:type> <tr:Reply> and $s <tucana:is> <${inReplyTo}> "
    + " and $s <tr:root> <${root}>;").replaceAll("\\Q${MODEL}", MODEL);

  //
  private static final String ITQL_DELETE_ID =
    ("delete select $s $p $o from ${MODEL} where $s $p $o and $s <tucana:is> <${id}> from ${MODEL};")
     .replaceAll("\\Q${MODEL}", MODEL);

  //
  private static final String ITQL_GET_DELETE_LIST_FOR_ID =
    ("select $s $b from ${MODEL} where $s <a:body> $b and "
    + " (walk ($c <tr:inReplyTo> <${id}> and $s <tr:inReplyTo> $c) or $s <tucanaL:is> <${id}>) "
    + " and $s <tr:root> $r and <${id}> <tr:root> $r;").replaceAll("\\Q${MODEL}", MODEL);

  //
  private static final String ITQL_GET_DELETE_LIST_IN_REPLY_TO =
    ("select $s $b from ${MODEL} where $s <a:body> $b and "
    + " walk ($c <tr:inReplyTo> <${inReplyTo}> and $s <tr:inReplyTo> $c) "
    + " and $s <tr:root> <${root}>;").replaceAll("\\Q${MODEL}", MODEL);

  //
  private static final String ITQL_GET =
    ("select $p $o from ${MODEL} where $s $p $o and "
    + "$s <r:type> <tr:Reply> and $s <tucana:is> <${id}>;").replaceAll("\\Q${MODEL}", MODEL);

  //
  private static final String ITQL_LIST_REPLIES =
    ("select $s subquery(select $p $o from ${MODEL} where $s $p $o) from ${MODEL}"
    + " where $s <tr:inReplyTo> <${inReplyTo}> and $s <tr:root> <${root}>"
    + " and $s <r:type> <tr:Reply>;").replaceAll("\\Q${MODEL}", MODEL);

  //
  private static final String ITQL_LIST_ALL_REPLIES =
    ("select $s subquery(select $p $o from ${MODEL} where $s $p $o) from ${MODEL}"
    + " where walk($c <tr:inReplyTo> <${inReplyTo}> and $s <tr:inReplyTo> $c)"
    + " and $s <tr:root> <${root}> and $s <r:type> <tr:Reply>;").replaceAll("\\Q${MODEL}", MODEL);

  static {
    aliases.put("a", AnnotationModel.a.toString());
    aliases.put("r", AnnotationModel.r.toString());
    aliases.put("d", AnnotationModel.d.toString());
    aliases.put("tr", ReplyModel.tr.toString());
  }

  private final RepliesPEP   pep;
  private final ItqlHelper   itql;
  private final FedoraHelper fedora;
  private final String       user;
  private final String       baseURI;

  /**
   * Creates a new RepliesImpl object.
   *
   * @param pep The xacml pep
   * @param itql The itql service
   * @param fedoraServer The fedora server uri
   * @param apim Fedora API-M stub
   * @param uploader Fedora uploader stub
   * @param user The authenticated user
   * @param user Base URI for generating reply ids
   */
  public RepliesImpl(RepliesPEP pep, ItqlHelper itql, URI fedoraServer, FedoraAPIM apim,
                     Uploader uploader, String user, String baseURI) {
    this.pep       = pep;
    this.itql      = itql;
    this.fedora    = new FedoraHelper(fedoraServer, apim, uploader);
    this.user      = (user == null) ? "anonymous" : user;
    this.baseURI   = baseURI;

    itql.setAliases(aliases);
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#createReply
   */
  public String createReply(String type, String root, String inReplyTo, String title, String body)
                     throws NoSuchIdException, RemoteException {
    itql.validateUri(body, "body");

    return createReply(type, root, inReplyTo, title, body, null, null);
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#createReply
   */
  public String createReply(String type, String root, String inReplyTo, String title,
                            String contentType, byte[] content)
                     throws NoSuchIdException, RemoteException {
    if (contentType == null)
      throw new NullPointerException("'contentType' cannot be null");

    if (content == null)
      throw new NullPointerException("'content' cannot be null");

    return createReply(type, root, inReplyTo, title, null, contentType, content);
  }

  private String createReply(String type, String root, String inReplyTo, String title, String body,
                             String contentType, byte[] content)
                      throws NoSuchIdException, RemoteException {
    if (type == null)
      type = "http://www.w3.org/2001/12/replyType#Comment";
    else
      itql.validateUri(type, "type");

    URI rootUri      = itql.validateUri(root, "root");
    URI inReplyToUri = itql.validateUri(inReplyTo, "inReplyTo");

    checkInReplyTo(rootUri, inReplyToUri);

    pep.checkAccess(pep.CREATE_REPLY, rootUri);

    if (!inReplyToUri.equals(rootUri))
      pep.checkAccess(pep.CREATE_REPLY, inReplyToUri);

    String id = getNextId();

    if (body == null) {
      body = fedora.createBody(contentType, content, "Reply", "Reply Body");

      if (log.isDebugEnabled())
        log.debug("created fedora object " + body + " for reply " + id);
    }

    String create = ITQL_CREATE;
    Map    values = new HashMap();

    values.put("id", id);
    values.put("type", type);
    values.put("root", root);
    values.put("inReplyTo", inReplyTo);
    values.put("body", body);
    values.put("user", user);
    values.put("created", itql.getUTCTime());

    if (title != null) {
      values.put("title", itql.escapeLiteral(title));
      create += ITQL_INSERT_TITLE;
    }

    itql.doUpdate(itql.bindValues(create, values));

    if (log.isDebugEnabled())
      log.debug("created reply " + id + " for " + inReplyTo + " with root " + root + " with body "
                + body);

    return id;
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#deleteReplies
   */
  public void deleteReplies(String root, String inReplyTo)
                     throws NoSuchIdException, RemoteException {
    checkInReplyTo(itql.validateUri(root, "root"), itql.validateUri(inReplyTo, "inReplyTo"));

    String txn   = "delete replies to " + inReplyTo;
    String query = ITQL_GET_DELETE_LIST_IN_REPLY_TO;
    query = query.replaceAll("\\Q${root}", root).replaceAll("\\Q${inReplyTo}", inReplyTo);

    doDelete(txn, query);
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#deleteReplies
   */
  public void deleteReplies(String id) throws NoSuchIdException, RemoteException {
    pep.checkAccess(pep.DELETE_REPLY, checkId(itql.validateUri(id, "id")));

    String txn   = "delete " + id;
    String query = ITQL_GET_DELETE_LIST_FOR_ID.replaceAll("\\Q${id}", id);

    doDelete(txn, query);
  }

  private void doDelete(String txn, String query) throws RemoteException {
    String[] purgeList;

    try {
      itql.beginTxn(txn);

      Answer ans = new Answer(itql.doQuery(query));

      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      int    c    = rows.size();
      List   pids = new ArrayList(c);

      for (int i = 0; i < c; i++) {
        Object[]     cols = (Object[]) rows.get(i);
        URIReference ref = (URIReference) cols[0];
        URI          id  = ref.getURI();

        ref = (URIReference) cols[1];

        String body = ref.getURI().toString();

        if (log.isDebugEnabled())
          log.debug("deleting " + id + " as part of " + txn);

        pep.checkAccess(pep.DELETE_REPLY, id);

        if (body.startsWith("info:fedora"))
          pids.add(fedora.uri2PID(body));

        String del = ITQL_DELETE_ID.replaceAll("\\Q${id}", id.toString());
        itql.doUpdate(del);
      }

      itql.commitTxn(txn);
      txn         = null;
      purgeList   = (String[]) pids.toArray(new String[0]);
    } catch (AnswerException e) {
      throw new RemoteException("Query failed", e);
    } finally {
      try {
        if (txn != null) {
          if (log.isDebugEnabled())
            log.debug("failed to " + txn);

          itql.rollbackTxn(txn);
        }
      } catch (Throwable t) {
      }
    }

    fedora.purgeObjects(purgeList);
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#getReplyInfo
   */
  public ReplyInfo getReplyInfo(String id) throws NoSuchIdException, RemoteException {
    pep.checkAccess(pep.GET_REPLY_INFO, itql.validateUri(id, "id"));

    try {
      String query = ITQL_GET.replaceAll("\\Q${id}", id);

      Answer ans  = new Answer(itql.doQuery(query));
      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      if (rows.size() == 0)
        throw new NoSuchIdException(id);

      return buildReplyInfo(id, rows);
    } catch (AnswerException e) {
      throw new RemoteException("Query failed", e);
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#listReplies
   */
  public ReplyInfo[] listReplies(String root, String inReplyTo)
                          throws NoSuchIdException, RemoteException {
    pep.checkAccess(pep.LIST_REPLIES,
                    checkInReplyTo(itql.validateUri(root, "root"),
                                   itql.validateUri(inReplyTo, "inReplyTo")));

    try {
      String query =
        ITQL_LIST_REPLIES.replaceAll("\\Q${root}", root).replaceAll("\\Q${inReplyTo}", inReplyTo);

      Answer ans = new Answer(itql.doQuery(query));

      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      return buildReplyInfoList(rows);
    } catch (AnswerException e) {
      throw new RemoteException("Query failed", e);
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#listAllReplies
   */
  public ReplyInfo[] listAllReplies(String root, String inReplyTo)
                             throws NoSuchIdException, RemoteException {
    pep.checkAccess(pep.LIST_ALL_REPLIES,
                    checkInReplyTo(itql.validateUri(root, "root"),
                                   itql.validateUri(inReplyTo, "inReplyTo")));

    try {
      String query =
        ITQL_LIST_ALL_REPLIES.replaceAll("\\Q${root}", root).replaceAll("\\Q${inReplyTo}", inReplyTo);

      Answer ans = new Answer(itql.doQuery(query));

      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      return buildReplyInfoList(rows);
    } catch (AnswerException e) {
      throw new RemoteException("Query failed", e);
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#createReply
   */
  public ReplyThread getReplyThread(String rootId, String inReplyTo)
                             throws NoSuchIdException, RemoteException {
    ReplyInfo[] replies = listAllReplies(rootId, inReplyTo);

    HashMap     map = new HashMap(replies.length * 2);

    for (int i = 0; i < replies.length; i++) {
      ReplyModel reply = (ReplyModel) replies[i];

      try {
        map.put(URI.create(reply.getId()), reply.clone(ReplyThread.class));
      } catch (InstantiationException e) {
        throw new Error(e);
      } catch (IllegalAccessException e) {
        throw new Error(e);
      }
    }

    URI         rootUri = URI.create(inReplyTo);
    ReplyThread root = new ReplyThread();
    root.setRoot(rootId);
    root.setId(inReplyTo);

    for (int i = 0; i < replies.length; i++) {
      URI         id    = URI.create(replies[i].getId());
      ReplyThread reply = (ReplyThread) map.get(id);

      URI         uri    = URI.create(reply.getInReplyTo());
      ReplyThread parent = uri.equals(rootUri) ? root : (ReplyThread) map.get(uri);

      if (parent == null)
        throw new Error("can't find " + uri + " replied by " + id);

      ReplyThread[] cur = parent.getReplies();
      ReplyThread[] nu;

      if (cur == null)
        nu = new ReplyThread[1];
      else {
        nu = new ReplyThread[cur.length + 1];
        System.arraycopy(cur, 0, nu, 0, cur.length);
      }

      nu[nu.length - 1] = reply;
      parent.setReplies(nu);
    }

    return root;
  }

  private URI checkId(URI id) throws RemoteException, NoSuchIdException {
    try {
      String query = ITQL_CHECK_ID.replaceAll("\\Q${id}", id.toString());
      Answer ans  = new Answer(itql.doQuery(query));
      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      if (rows.size() == 0)
        throw new NoSuchIdException(id.toString());

      return id;
    } catch (AnswerException e) {
      throw new RemoteException("Query failed", e);
    }
  }

  private URI checkInReplyTo(URI root, URI inReplyTo) throws RemoteException, NoSuchIdException {
    if (inReplyTo.equals(root))
      return inReplyTo;

    try {
      String query =
        ITQL_CHECK_IN_REPLY_TO.replaceAll("\\Q${root}", root.toString()).replaceAll("\\Q${inReplyTo}",
                                                                                    inReplyTo
                                                                                     .toString());
      Answer ans  = new Answer(itql.doQuery(query));
      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      if (rows.size() == 0)
        throw new NoSuchIdException(inReplyTo.toString());

      return inReplyTo;
    } catch (AnswerException e) {
      throw new RemoteException("Query failed", e);
    }
  }

  private ReplyInfo buildReplyInfo(String id, List rows) {
    ReplyInfo info = ReplyModel.create(id, rows);
    info.setBody(fedora.getBodyURL(info.getBody()));

    return info;
  }

  private ReplyInfo[] buildReplyInfoList(List rows) {
    ReplyInfo[] replies = new ReplyInfo[rows.size()];

    for (int i = 0; i < replies.length; i++) {
      Object[]           cols           = (Object[]) rows.get(i);
      URIReference       ref            = (URIReference) cols[0];
      String             id             = ref.getURI().toString();
      Answer.QueryAnswer subQueryAnswer = (Answer.QueryAnswer) cols[1];
      replies[i] = buildReplyInfo(id, subQueryAnswer.getRows());
    }

    return replies;
  }

  private String getNextId() throws RemoteException {
    return baseURI + fedora.getNextId(REPLY_PID_NS).replace(':', '/');
  }
}
