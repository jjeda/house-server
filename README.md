## 0. 아키텍쳐

![team31Backend](src/main/resources/static/team31Backend.jpg)



## 1. 기술 스택

- Spring security(OAuth2) : 인증, 권한, 인가
  - 초기 Grant Type : password , TokenStore : Inmemory(JVM)
  - 확장 Grant Type : Authorization Code, TokenStore : JVM -> JDBC -> Redis 
- Spring Boot
- Spring MVC
- Spring data JPA : ORM
- Spring HATEOAS : RESTful
- Spring REST Docs : RESTful



## 2. 패러다임

- TDD[(테스트코드 보기)](/src/test/java/me/jjeda/houseserver/boards/BoardControllerTest.java)
  - Unit Test
  - Slicing Test
  - Application Test
- REST
  - Self-descriptive
  - HATEOAS
- MSA // TODO
  - 까지 할 수 있을까..
  - Docker
  - AWS // http://54.180.36.19:8080
  - API Gateway
  - Jenkins

## 3. [DOCS](http://54.180.36.19:8080/docs/index.html) 
![](src/main/resources/static/docs/docs1.png)
![](src/main/resources/static/docs/docs9.png)

## 4. 인증과정
- /oauth/token 로 POST 요청
  - basic 요청으로 clientId("myApp")와 clientPass("pass") 를 포함
  - "username", "password", "grant_type"
  - "username" 은 사용자의 email, "password"는 비밀번호 , "grant_type"은 "password"로 고정
```http request
Headers =
[
  Authorization:"Basic bXlBcHA6cGFzcw=="
]
Parameters =
{
  username : "[user@email.com]", 
  password : "[user]", 
  grant_type : "password"
}
```
  
```json
// 토큰 예시
{ 
  "access_token":"f253ac88-d30b-498e-8f8b-abec71d64881",
  "token_type":"bearer",
  "refresh_token":"3e986a5d-5422-49f9-a4c3-4c3aeb8230fe",
  "expires_in":599,
  "scope":"read write",
}
```

- 인증이 필요한 로직에 접근할 때 Header에 token_type + access_token을 포함시킨다
```http request
Headers =
[
  Content-Type:"application/json;charset=UTF-8", 
  Authorization:"bearer f253ac88-d30b-498e-8f8b-abec71d64881", 
  Accept:"application/hal+json"
]
``` 

## 5. API 접근 flow
- form data or axios 등으로 /api 에 GET 요청을 보낸다.
```json
/* Response Message */
{
  "_links": {
    "boards": {
      "href":"http://localhost:8080/api/boards"
    }
  }
}
```
- Response message의 _links.boards.href 를 통해 GET 요청을 보내면 다음과 같은 메시지를 받는다.
```json
{
    "_links": {
        "self": {
            "href": "http://localhost:8080/api/boards?page=0&size=20"
        },
        "profile": {
            "href": "/docs/index.html#resources-boards-list"
        },
        /* 로그인 했을 때만 보여짐 */
        "create-board": {
            "href": "http://localhost:8080/api/boards"
        }
    },
    "page": {
        "size": 20,
        "totalElements": 0,
        "totalPages": 0,
        "number": 0
    }
}
```
- _links.create-board 를 통해 게시물 생성가능
```json
/* Request Message */
{
	"title" : "Test Title",
	"contents" : "test contents",
	"boardType" :"PORTFOLIO"
}
```
```json
/* Response Message */
{
    "id": 3,
    "title": "Test Title",
    "contents": "test contents",
    "createdDateTime": "2019-08-05T14:25:56.6192144",
    "modifiedDateTime": null,
    "boardType": "PORTFOLIO",
    "manager": {
        "id": 1,
        "role": "[USER]"
    },
    "_links": {
        "self": {
            "href": "http://localhost:8080/api/boards/3"
        },
        "query-boards": {
            "href": "http://localhost:8080/api/boards"
        },
        "update-board": {
            "href": "http://localhost:8080/api/boards/3"
        },
        "delete-board": {
            "href:": "http://localhost:8080/api/boards/3"
        },
        "profile": {
            "href": "/docs/index.html#resources-boards-create"
        }
    }
}
```
- 응답받은 메시지의 URI로 CRUD 를 할 수 있다.
