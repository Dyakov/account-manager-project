# account-manager-project
Для управления пользователями API нет.
Банковские счета могут быть активными и заблокированными
Для активного банковского счёта доступны следующие операции:
1. удаление счёта
2. внесение денег на счёт
3. снятие дененг со счёта
4. блокировка счёта

Для заблокированного банковского счёта доступны следующие операции:
1. удаление счёта
2. активизация счёта

Примеры запросов для командной строки Windows:
1. Получить все банковские счета (в реальном проекте
добавил бы разбиение на страницы):
curl -v localhost:8080/bank/accounts
2. Получить банковский счёт по идентификатору:
curl -v localhost:8080/bank/accounts/1
3. Добавить новый банковский счёт для заданного
пользователя:
curl -v "localhost:8080/bank/accounts" -H "Content-Type:application/json" -d "{\"id\":1,\"name\":\"Vladimir\",\"surname\":\"Dyakov\"}"
4. Удалить банковский счёт по идентификатору:
curl -v -X DELETE "localhost:8080/bank/accounts/5/delete"
5. Заблокировать банковский счёт по идентификатору:
curl -v -X DELETE "localhost:8080/bank/accounts/3/block"
6. Активировать банковский счёт по идентификатору:
curl -v -X PUT "localhost:8080/bank/accounts/3/activate"
7. Внесение денег на счёт:
curl -v -X PUT "localhost:8080/bank/accounts/3/deposit/money" -H "Content-type:application/json"  -d "{\"amount\":55.4}"
8. Снятие денег со счёта:
curl -v -X PUT "localhost:8080/bank/accounts/3/withdraw/money" -H "Content-type:application/json"  -d "{\"amount\":55.4}"
9. Перевод денег со счёта на счёт:
curl -v -X PUT "localhost:8080/bank/accounts" -H "Content-type:application/json"  -d "{\"bankAccountIdFrom\":3, \"bankAccountIdTo\":1,\
"amount\":10.05}"




