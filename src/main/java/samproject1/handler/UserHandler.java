package samproject1.handler;

import com.bardframework.bard.basic.marker.Doc;
import com.bardframework.bard.basic.marker.ErrorCase;
import com.bardframework.bard.basic.marker.HandleErrors;
import com.bardframework.bard.basic.marker.Required;
import com.bardframework.bard.core.Handler;
import com.bardframework.bard.core.marker.Model;
import com.bardframework.bard.util.db.marker.DBSession;
import com.bardframework.bard.util.user.PasswordEncrypter;
import com.bardframework.bard.util.user.TokenStorage;
import com.bardframework.bard.util.user.marker.LoginUser;
import com.mysema.query.jpa.impl.JPAQuery;
import samproject1.model.QUser;
import samproject1.model.User;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/user")
public class UserHandler extends Handler {

    @DBSession public EntityManager em;

    @Doc("Sign up a user")
    @GET
    @Path("/signup")
    @HandleErrors({
        @ErrorCase(code = 20000,
            exception = UsernameDuplicateException.class,
            description = "Username duplicate")
    })
    public User signup(
        @QueryParam("username") @Required String username,
        @QueryParam("password") @Required String password,
        @QueryParam("email") String email
    ) throws UsernameDuplicateException {
        JPAQuery query = new JPAQuery(em);
        QUser quser = QUser.user;
        User user = query.from(quser).where(quser.username.eq(username)).uniqueResult(quser);
        if (user != null) {
            throw new UsernameDuplicateException(username);
        }
        user = new User();
        user.username = username;
        user.email = email;
        String[] result = PasswordEncrypter.encrypt(password);
        user.password = result[0];
        user.salt = result[1];
        em.persist(user);
        return user;
    }

    @Doc("User login. Returns the auth token.")
    @GET
    @Path("/login")
    @HandleErrors({
        @ErrorCase(code = 20001,
            exception = InvalidatePasswordException.class,
            description = "invalidate password")
    })
    public TokenResult login(
        @QueryParam("username") @Required String username,
        @QueryParam("password") @Required String password
    ) throws InvalidatePasswordException {
        QUser quser = QUser.user;
        User user =
            new JPAQuery(em).from(quser).where(quser.username.eq(username)).uniqueResult(quser);
        if (user == null || !user.password.equals(PasswordEncrypter.encrypt(password, user.salt))) {
            throw new InvalidatePasswordException();
        }
        TokenResult result = new TokenResult();
        result.token = TokenStorage.put(user.id, 60 * 60 * 24);
        return result;
    }

    @Doc("Get the user info")
    @GET
    @Path("/info")
    public User info(@LoginUser(required = true) String id) {
        return em.find(User.class, id);
    }

    @Model
    public static class TokenResult {
        public String token;
    }


    public static class UsernameDuplicateException extends Exception {
        public UsernameDuplicateException(String username) {
            super("Username duplicate: " + username);
        }
    }


    public static class InvalidatePasswordException extends Exception {
        public InvalidatePasswordException() {
            super("Invalidate username or password");
        }
    }
}
