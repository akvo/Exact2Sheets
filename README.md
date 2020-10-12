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

Pending issues:

* Date formatting is not quite right, each time a date new is inserted the formatting is lost
* There should be a cron job to run the tasks daily
* The "/api/v1/$division/read/financial/OutstandingInvoicesOverview is not the one we expected and does not provide a list of outstanding invoices, we need to look for something else
