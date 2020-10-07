# Exact2Sheets

Experimental


Create a file named `secrets.conf` inside `resources` folder and add the right values:

ktor {
    secret {
      clientId = "your client id"
      clientSecret = "your client secret"
      googleToken = "your temporary google auth token"
    }
}
