
## Create Secrets in Vault
```bash
docker exec -it vault-dev sh
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN='dev-root-token'

# Now run your command
vault kv put secret/pdf-converter \
  database.username=dbuser \
  database.password=dbpass123 \
  database.url=jdbc:postgresql://localhost:5432/mydb \
  api.key=my-secret-api-key \
  api.endpoint=https://api.example.com
```