package samproject1.handler;


import com.bardframework.bard.basic.marker.*;
import com.bardframework.bard.core.Handler;
import com.bardframework.bard.util.db.marker.DBSession;
import com.bardframework.bard.util.user.marker.LoginUser;
import samproject1.model.Article;
import samproject1.model.User;
import org.hibernate.ObjectNotFoundException;

import javax.persistence.EntityManager;
import javax.ws.rs.*;


@Path("/article")
public class ArticleHandler extends Handler {
    @DBSession public EntityManager em;
    @LoginUser(required = true) public String userId;

    @PUT
    @Path("/")
    @Doc("Create an article")
    public Article createArticle(
        @FormParam("title") @Required String title,
        @FormParam("content") @EscapeHTML @Required String content
    ) {
        Article article = new Article();
        article.author = em.find(User.class, userId);
        article.title = title;
        article.content = content;
        em.persist(article);
        return article;
    }

    @HandleErrors({
        @ErrorCase(code = 20010, exception = ObjectNotFoundException.class,
            description = "article not found")
    })
    @GET
    @Path("/{id}")
    @Doc("Get an article by id")
    public Article getArticle(@PathParam("id") @Required String articleId) {
        return em.find(Article.class, articleId);
    }
}
