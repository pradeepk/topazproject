<div id="discussionContainer">

  <div id="content">
    <h1>Ratings</h1>
      <div class="source">
        <span>Original Article</span>
        <a href="/article/fetchArticle.action?articleURI=${articleURI}" title="Back to original article" class="article icon">${articleTitle}
          <span class="inline-rating inlineRatingEnd">
            <ul class="star-rating pone_rating" title="overall">
              <li class="current-rating overall-rating pct70">TODO: re-calc ratings summary? best to refactor ratings action(s)?</li>
            </ul>
          </span>
        </a>
        <p><a href="/annotation/getCommentary.action?target=${articleURI}" class="commentary icon">See all commentary</a> on this article</p>
      </div>

      <div class="rsep"></div>

      <#list articleRatings as articleRatingSummary>
        <div class="response ratingComment">
          <div class="hd">
            <!-- begin : response title : user -->
            <h3><span class="detail">Posted by <a href="/user/showUser.action?userId=${articleRatingSummary.creatorURI}" title="Annotation Author" class="user icon">${articleRatingSummary.creatorName}</a></span></h3>
            <!-- end : response title : user -->
          </div>
          <!-- begin : response body text -->
          <div class="ratingDetail">
            <div class="posterRating">
              <ol class="ratingAvgs">
                <#if articleRatingSummary.insight?exists>
                  <li><label for="insight">Insight</label>
                      <ul class="star-rating pone_rating" title="insight">
                        <#assign insightPct = (20 * articleRatingSummary.insight)?string("##0")>
                        <li class="current-rating average pct${insightPct}">Currently ${articleRatingSummary.insight?string("0.#")}/5 Stars.</li>
                      </ul>
                  </li>
                </#if>
                <#if articleRatingSummary.reliability?exists>
                  <li><label for="reliability">Reliability</label>
                    <ul class="star-rating pone_rating" title="reliability">
                      <#assign reliabilityPct = (20 * articleRatingSummary.reliability)?string("##0")>
                      <li class="current-rating average pct${reliabilityPct}">Currently ${articleRatingSummary.reliability?string("0.#")}/5 Stars.</li>
                    </ul>
                  </li>
                </#if>
                <#if articleRatingSummary.style?exists>
                  <li><label for="style">Style</label>
                    <ul class="star-rating pone_rating" title="style">
                      <#assign stylePct = (20 * articleRatingSummary.style)?string("##0")>
                      <li class="current-rating average pct${stylePct}">Currently ${articleRatingSummary.style?string("0.#")}/5 Stars.</li>
                    </ul>
                  </li>
                </#if>
                <#if articleRatingSummary.overall?exists>
                  <li><label for="overall">Overall</label>
                    <ul class="star-rating pone_rating" title="overall">
                      <#assign stylePct = (20 * articleRatingSummary.overall)?string("##0")>
                      <li class="current-rating average pct${stylePct}">Currently ${articleRatingSummary.overall?string("0.#")}/5 Stars.</li>
                    </ul>
                  </li>
                </#if>
              </ol>
            </div>
            <blockquote>
              <#if articleRatingSummary.commentTitle?exists>
                <h4>${articleRatingSummary.commentTitle}</h4>
              </#if>
              <#if articleRatingSummary.commentValue?exists>
                <p>${articleRatingSummary.commentValue}</p>
              </#if>
            </blockquote>
          </div>
        </div>
      </#list>
    </div>
</div>