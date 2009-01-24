/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.ambra.admin.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;

public class CrossRefPosterService {
  private String doiXrefUrl;

  public void init() {
  }

  public void setDoiXrefUrl(final String doiXrefUrl) {
    this.doiXrefUrl = doiXrefUrl;
  }

  public int post(File file) throws HttpException, IOException {
    PostMethod poster = new PostMethod(doiXrefUrl);
    HttpClient client = new HttpClient();

    Part[] parts = {new FilePart("fname", file.getName(), file)};

    poster.setRequestEntity(new MultipartRequestEntity(parts, poster.getParams()));
    client.getHttpConnectionManager().getParams().setConnectionTimeout(25000);
    return client.executeMethod(poster);
  }
}
