## 0. 아키텍쳐

![team31Backend](src/main/resources/static/team31Backend.jpg)



## 1. 기술 스택

- Spring security(OAuth2) : 인증, 권한, 인가
  - 초기 Grant Type : password , TokenStore : Inmemmory(JVM)
  - 확장 Grant Type : Authorization Code, TokenStore : JDBC -> Redis 
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
- REST[(Docs 바로가기)](/static/docs/index.html) //TODO : 배포
  - Self-descriptive
  - HATEOAS
- MSA // TODO
  - 까지 할 수 있을까..
  - Docker
  - AWS or GCP
  - API Gateway
  - Jenkins

## 3. DOCS //  TODO : 배포
![](src/main/resources/static/docs/docs1.png)
![](src/main/resources/static/docs/docs3.png)
![](src/main/resources/static/docs/docs6.png)
![](src/main/resources/static/docs/docs7.png)
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
  username = [user@email.com], 
  password = [user], 
  grant_type = [password]
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

- 인증이 필요한 로직에 접근할 때 Header에 access_token을 포함시킨다
```http request
Headers =
[
  Content-Type:"application/json;charset=UTF-8", 
  Authorization:"Bearer f253ac88-d30b-498e-8f8b-abec71d64881", 
  Accept:"application/hal+json"
]
``` 