<a href="https://github.com/actions/toolkit"><img alt="GitHub Actions status" src="https://github.com/prokobit/tauron-client/workflows/build/badge.svg"></a>
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=prokobit_tauron-client&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=prokobit_tauron-client)
# Tauron Client
Java client to retrieve data about energy production and consumption from `elicznik.tauron-dystrybucja.pl`

# Getting Started
Required minimum Java 11, because client based on Java HTTP Client.

```
TauronClient client = TauronClient.create();
SessionContext sessionContext = tauronClient.login("test@mail.com", "pass");
DataForm form = DataForm.ofMonth(sessionContext, YearMonth.of(2022, Month.MARCH))
Chart chart = tauronClient.getChart(form);
```
Client has posibility to get data by different kind of period

`DataForm.ofDay(CounterId counterId, LocalDate date)`

`DataForm.ofMonth(CounterId counterId, YearMonth yearMonth)`

`DataForm.ofYear(CounterId counterId, Year year) `

`DataForm.ofPeriod(CounterId counterId, LocalDate startDay, LocalDate endDay)`

`DataForm.ofPeriod(CounterId counterId, LocalDate startDay, LocalDate endDay, Sampling type) `

# Good to know
Tauron has limit for logins per day, after limit exceeded, website requires captcha but this client not support this case.
Best practice is sign in only ones, save session and then request for data to avoid above situation. 

Use at Your Own Risk !


