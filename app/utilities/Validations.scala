package utilities 

/**
* Validations for all the data that will be handled throughout the application
*/
object Validations {
    val commonPasswords = Array("password", "123", "hello123", "password@123")

    /**
    * Check if the password is acceptable for use such as no common passwords, etc
    */
    def isPasswordAcceptable(password: String): Boolean = {
        password.length > 6 && !(commonPasswords contains password)
    }
}