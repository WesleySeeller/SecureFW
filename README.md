# 💰 Secure Finance

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/Compose-1.10.4-green.svg)
![Firebase](https://img.shields.io/badge/Firebase-Firestore%20%7C%20Auth-orange.svg)
![Room](https://img.shields.io/badge/Room-2.7.1-lightgrey.svg)
![Tests](https://img.shields.io/badge/Tests-Passing-brightgreen.svg)

O **Secure Finance** é um aplicativo Android de gerenciamento financeiro pessoal projetado com **foco absoluto em segurança e usabilidade offline-first**. Com ele, os usuários podem registrar suas receitas e despesas, categorizá-las e acompanhar o saldo em tempo real, com a tranquilidade de que seus dados estão criptografados e sincronizados de forma segura.

## 📱 Funcionalidades

- **Autenticação Segura**: Login e Cadastro com e-mail e senha via Firebase Auth.
- **Gestão de Transações**: Adicione, edite e exclua receitas e despesas.
- **Dashboard Inteligente**: Cálculo automático de saldo, total de receitas e total de despesas.
- **Sincronização Offline-First**: Os dados são salvos localmente via Room e sincronizados com o Firestore em segundo plano.
- **Validação de Dados**: Proteção contra inserção de dados inválidos nos formulários.
- **Sessão Persistente**: Mantém o usuário logado de forma segura.

## 🛠️ Tecnologias e Arquitetura

O projeto foi construído seguindo os padrões modernos de desenvolvimento Android, utilizando o que há de mais recente no ecossistema:

- **Linguagem**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Arquitetura**: MVVM + Clean Architecture (Data, Domain, Presentation)
- **Injeção de Dependência**: Hilt
- **Processamento de Anotações**: KSP (Kotlin Symbol Processing)
- **Persistência Local**: Room Database
- **Backend as a Service**: Firebase (Auth, Firestore, Analytics)
- **Assincronismo**: Coroutines e Flow

## 🔐 Segurança

- **Armazenamento Seguro**: Uso de `EncryptedSharedPreferences` para armazenar os dados sensíveis de sessão do usuário.
- *(Opcional: se você implementar)* **SSL Pinning** no consumo de APIs externas para evitar ataques Man-in-the-Middle.

## 🧪 Testes Automatizados

O projeto conta com uma suíte de testes para garantir a estabilidade do código:

- **Testes Unitários (JVM)**: ViewModels e Repositórios utilizando **JUnit**, **MockK**, **Truth** e **Turbine**.
- **Testes de Integração de Banco**: **Room In-Memory** + **Robolectric**.
- **Testes de UI (Compose)**: Telas de Login e Dashboard com `ComposeTestRule`.

Para rodar os testes:
```bash
# Testes unitários e de repositório
./gradlew testDebugUnitTest

# Testes de Interface (requer emulador)
./gradlew connectedDebugAndroidTest