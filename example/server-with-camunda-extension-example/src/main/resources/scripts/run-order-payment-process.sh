curl --location 'http://localhost:8080/api/v1/runtime/processes/key' \
--header 'accept: */*' \
--header 'Content-Type: application/json' \
--data '{
    "definitionKey": "OrderPaymentProcess",
    "variables": {
        "amount": 100,
        "order": {
            "number": 1234
        },
        "client": {
            "firstName": "Json",
            "lastName": "Statement"
        }
    }
}'