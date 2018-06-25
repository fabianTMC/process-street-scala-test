package utilities

import java.security.MessageDigest
import java.math.BigInteger

import org.mindrot.jbcrypt.BCrypt

/**
* Helper functions for everything password related
*/
object PasswordHelper {
    /**
    * Generate a random string of 16 characters for use as a salt
    */
    def generateSalt() = {
        BCrypt.gensalt()
    }

    /**
    * Hash the given password with the given salt using the given algorithm
    * @param password String [Required] the password to be hashed
    * @param salt String [Required] the salt to be used with the password 
    */
    def hashPassword(password: String, salt: String) = {
        BCrypt.hashpw(password, salt)
    }
}