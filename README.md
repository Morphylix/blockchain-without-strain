# BlockChain without strain

Master:
[![Tests](https://github.com/Morphylix/blockchain-without-strain/actions/workflows/gradle-tests.yml/badge.svg?branch=master)](https://github.com/Morphylix/blockchain-without-strain/actions/workflows/gradle-tests.yml)
Develop:
[![Tests](https://github.com/Morphylix/blockchain-without-strain/actions/workflows/gradle-tests.yml/badge.svg?branch=dev)](https://github.com/Morphylix/blockchain-without-strain/actions/workflows/gradle-tests.yml)

Собрать .jar:
- Windows gradlew.bat MyFatJar
Лежит в build/libs

Запуск:
```
java -jar com.example.blockchain-without-strain-0.0.1.jar 8080 8081 8082 1
java -jar com.example.blockchain-without-strain-0.0.1.jar 8081 8080 8082 0
java -jar com.example.blockchain-without-strain-0.0.1.jar 8082 8080 8081 0
```
Аргументы:
[0] - current node port
[1] - first node port
[2] - second node port
[3] - is current node main or not ("1" - main, "0" - secondary)

## Docker:
Собрать докер-образ: "docker build -t blockchain-without-strain .",  
запустить docker-compose: "docker-compose up"

# Тестирование

Для тестирования приложения были реализованы:

- Модульные тесты (testValidateGenesisManually, testValidateBlockChainManually);
- Интеграционные (testBlockInsertedNotification, testBlockInsertedNotificationFailed, testAskThirdNode, testSendLastBlock, testValidateBlockChain, testValidateBlockChainFailed, testHandleReceivedBlock)

Был создан runner для Github, тестирование проводится под различные ОС: Linux, Windows, MacOS.