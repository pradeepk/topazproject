            <h6>Average Rating <a href="" class="rating">(36 User Ratings)</a></h6>
            <ol>
              <li>
              	<span class="inline-rating">
				  <ul class="star-rating pone_rating" title="overall">
				    <li class="current-rating average-rating pct60">Currently 3/5 Stars.</li>
				  </ul>		
				</span>	
                <a href="javascript:void(0);" onclick="return topaz.domUtil.swapDisplayMode('ratingAverages');" class="rating catAvg">See all categories</a>
                  <fieldset id="ratingAverages">
                    <ol class="ratingAvgs">
                      <li><label for="insight">Insight</label>
						<ul class="star-rating pone_rating" title="insight">
						  <li class="current-rating average pct60">Currently 3/5 Stars.</li>
						</ul>		
                      </li>
                      <li><label for="reliability">Reliability</label>
						<ul class="star-rating pone_rating" title="reliability">
						  <li class="current-rating average pct30">Currently 1.5/5 Stars.</li>
						</ul>		
                      </li>
                      <li><label for="style">Style</label>
			        	<ul class="star-rating pone_rating" title="style">
						  <li class="current-rating average pct80">Currently 4/5 Stars.</li>
						</ul>		
                      </li>
                    </ol>
                  </fieldset>
                </li>
                <li>
                  <#if Session.PLOS_ONE_USER?exists>
                    <!-- // Create a new rating 
                    <a href="javascript:void(0);" onclick="return topaz.rating.show();" class="rating">Rate This Article</a>
                    -->
                    <!-- // Edit rating -->
                    <a href="javascript:void(0);" onclick="return topaz.rating.show('edit');" class="rating">Edit My Rating</a>
                    
                  <#else>
                    <a href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}" class="rating">Rate This Article</a>
                  </#if>
                </li>
              </ol>
