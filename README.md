# Exact2Sheets

Experimental


Add the files `secret.conf` and `credentials.json` inside `resources` folder, you will find them in the vault. Alternatively this is the `secrets.conf` template

```
ktor {
    secret {
      clientId = "your client id"
      clientSecret = "your client secret"
      googleToken = "your temporary google auth token"
    }
}
```

and the `secrets.json` file is the google service account key.
