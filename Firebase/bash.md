# POST
```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"nombre": "Empanada", "descripcion": "Empanada de carne jugosa", "precio": 8.0, "stock": 15}' \
  "https://classfirebase-d856a-default-rtdb.firebaseio.com/productos.json"
  ```
# PATCH
```
curl -X POST -H "Content-Type: application/json" \
  -d '{"nombre": "Empanada", "descripcion": "Empanada de carne jugosa", "precio": 8.0, "stock": 15}' \
  "https://classfirebase-d856a-default-rtdb.firebaseio.com/productos.json"
  ```
# DELETE
```
curl -X DELETE \
  "https://classfirebase-d856a-default-rtdb.firebaseio.com/productos/-Nseed004.json"
  ```
