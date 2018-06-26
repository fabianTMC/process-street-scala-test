# process-street-scala-test

## Running

Please update the `db.default` configuration in `application.conf`

And then go to <http://localhost:9000> to see the running web application.

## API Details

### Users
#### Create User
`POST /create`
```json
{
	"email": "email@email.com",
	"password": "password123"
}
```

#### Login User
`POST /login`
```json
{
	"email": "email@email.com",
	"password": "password123"
}
```

### Todos
#### Create todo (Requires JWT in Authorization Header)
`POST /todos/create`
```json
{
	"text": "Hello World"
}
```

#### List all todos of the user (Requires JWT in Authorization Header)
`POST /todos/list`

#### Edit todo (Requires JWT in Authorization Header)
`POST /todos/edit`
```json
{
	"text": "Hello World Updated",
  "uuid": "uuid-of-todo"
}
```

#### Toggle todo checked (Requires JWT in Authorization Header)
`POST /todos/toggleChecked`
```json
{
	"checked": true|false,
  "uuid": "uuid-of-todo"
}
```

#### Delete todo (Requires JWT in Authorization Header)
`POST /todos/delete`
```json
{
  "uuid": "uuid-of-todo"
}
```

### Comments
#### Create comment (Requires JWT in Authorization Header)
`POST /comments/create`
```json
{
	"text": "First Comment",
  "todo": "uuid-of-todo"
}
```

#### List all comments of the given todo (Requires JWT in Authorization Header)
`POST /comments/list`
```json
{
  "todo": "uuid-of-todo"
}
```

#### Edit todo (Requires JWT in Authorization Header)
`POST /comments/edit`
```json
{
	"text": "Hello World Updated",
  "uuid": "uuid-of-comment"
}
```

#### Delete todo (Requires JWT in Authorization Header)
`POST /comments/delete`
```json
{
  "uuid": "uuid-of-comment"
}
```
