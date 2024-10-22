## 學號：M1325977

## 姓名：廖書賢

## 設置

### package

```
// use vscode
// In Java Project Referenced Libraries, add below packages(in packages folder)
java-dotenv-5.2.2.jar
kotlin-stdlib-1.4.0.jar
```

### env

```
// create .env file in project
參考 .env.example
```

## 啟動

### Windows

```
執行 Demo.exe
```

### Mac

```bash
java -jar Demo.jar
```

## 輸入範例

```
Local mailserver: smtp.gmail.com
From: xxx@gmail.com
To: xxx@gmail.com
Subject: 測試郵件
Ｍessage: 這是一封使用 Google SMTP 傳給您的測試郵件
```

## 補充

### SMTP 回應碼

![smtp code](./img/smtp%20code.png)

235: 用戶驗證成功
334: 等待用戶輸入驗證訊息

### SMTP 格式

- SMTP 響應通常以三位數字程式碼開始
- 如果響應跨多行，除最後一行外，每行的第 4 個字符是 "-"
