package javalinstagram.account

import io.javalin.BadRequestResponse
import io.javalin.Context
import io.javalin.UnauthorizedResponse
import io.javalin.core.util.Header
import javalinstagram.currentUser
import org.mindrot.jbcrypt.BCrypt

data class Credentials(val username: String = "", val password: String = "")

object AccountController {

    fun signIn(ctx: Context) {
        val (username, password) = ctx.validatedBody<Credentials>()
                .check({ it.username.isNotBlank() && it.password.isNotBlank() })
                .getOrThrow()
        val user = AccountDao.findById(username)
        if (user != null && BCrypt.checkpw(password, user.password)) {
            ctx.status(200)
            ctx.currentUser = username
        } else {
            throw UnauthorizedResponse("Incorrect username/password")
        }
    }

    fun signUp(ctx: Context) {
        val (username, password) = ctx.validatedBody<Credentials>()
                .check({ it.username.isNotBlank() && it.password.isNotBlank() })
                .getOrThrow()
        val user = AccountDao.findById(username)
        if (user == null) {
            AccountDao.add(id = username, password = BCrypt.hashpw(password, BCrypt.gensalt()))
            ctx.status(201)
            ctx.currentUser = username
        } else {
            throw BadRequestResponse("Username '$username' is taken")
        }
    }

    fun signOut(ctx: Context) {
        ctx.currentUser = null
        if (ctx.header(Header.ACCEPT)?.contains("html") == true) {
            ctx.redirect("/signin")
        }
    }

}
