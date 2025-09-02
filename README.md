# uaiPapo 💬

**Aplicativo de mensagens instantâneas em tempo real para Android**

Desenvolvido como projeto acadêmico para a disciplina de Programação para Dispositivos Móveis da Universidade Federal de Uberlândia (UFU).

## 👥 Equipe de Desenvolvimento

- **Chloe Anne Scaramal** - 12311BSI232
- **Gabriel Augusto Paiva** - 12311BSI245
- **João Pedro Zanetti** - 12311BSI230  
- **Rayane Reis Mota** - 12311BSI233
- **Vinicius Resende Garcia** - 12021BCC027

## 📦 Entregáveis

Os arquivos entregáveis estão na pasta deliverable/ na raiz do repositório:
```
deliverable
├── cronograma.pdf
├── quiz-app.apk
├── relatório.pdf
├── slides.pdf
└── vídeo.mp4
```

## 📱 Sobre o Projeto

O uaiPapo é um aplicativo de mensagens instantâneas desenvolvido nativamente para Android que permite comunicação em tempo real entre usuários, oferecendo uma experiência completa de chat com funcionalidades modernas e interface intuitiva.

## ✨ Funcionalidades Principais

### 🔐 Autenticação e Cadastro
- Login e cadastro via **email/senha** ou **telefone**
- Recuperação de senha integrada
- Validação OTP para número de telefone
- Perfis de usuário personalizáveis

### 💬 Sistema de Mensagens
- **Mensagens em tempo real** com sincronização instantânea
- Suporte a **texto e imagens**
- Status de mensagens (enviada, entregue, lida)
- **Notificações push** personalizadas
- Histórico de conversas persistente

### 👥 Gerenciamento de Contatos
- Busca de usuários por nome
- Importação de contatos do dispositivo
- Lista de contatos que são usuários do app
- Indicador de status online/offline/ocupado

### 🏷️ Grupos de Conversa
- Criação e gerenciamento de grupos
- Adição/remoção de participantes
- Renomeação de grupos
- Configurações personalizadas de notificação

### 🔍 Sistema de Busca
- **Busca global de mensagens** em todas as conversas
- **Filtro de mensagens por palavra-chave** dentro do chat
- Navegação entre resultados de busca
- Destaque visual de termos encontrados

### 📌 Funcionalidades Especiais
- **Mensagens fixadas**: Permite fixar mensagens importantes no topo do chat
- **Filtro de mensagens**: Sistema avançado de busca e filtragem dentro das conversas

### 📢 Sistema de Transmissão
- Envio de mensagens para múltiplos contatos simultaneamente
- Seleção personalizada de destinatários
- Interface dedicada para composição de transmissões

### 🎨 Interface e Experiência
- Design moderno e intuitivo
- Modo escuro/claro
- Indicadores de status personalizados
- Animações fluidas e responsivas
- Suporte a emojis

### 🔒 Segurança e Privacidade
- Gerenciamento de sessões múltiplas
- Armazenamento seguro de dados

### 📱 Funcionalidades Técnicas
- **Sincronização em tempo real** usando Firebase Firestore
- **Armazenamento offline** com sincronização automática
- Upload e visualização de imagens
- Notificações push usando Firebase Cloud Messaging
- Arquitetura robusta com padrões Android modernos

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: Java
- **Plataforma**: Android (API 26+)
- **Backend**: Firebase
  - Firestore (banco de dados)
  - Authentication (autenticação)
  - Storage (armazenamento de arquivos)
  - Cloud Messaging (notificações push)
- **Bibliotecas**:
  - Material Design Components
  - Glide (carregamento de imagens)
  - Firebase UI
  - Country Code Picker
  - Image Picker

## 📋 Requisitos do Sistema

- Android 8.0 (API 26) ou superior
- Conexão com a Internet
- Permissões:
  - Acesso à Internet
  - Leitura de contatos (opcional)
  - Acesso à câmera e galeria (para envio de imagens)

## 🚀 Instalação

1. Clone o repositório
2. Abra o projeto no Android Studio
3. Configure o Firebase:
   - Adicione o arquivo `google-services.json` na pasta `app/`
   - Configure as chaves de autenticação necessárias
4. Compile e execute o projeto

## 📊 Estrutura do Projeto

```
app/
├── src/main/java/com/example/uaipapo/
│   ├── adapter/          # Adaptadores para RecyclerView
│   ├── model/           # Modelos de dados
│   ├── utils/           # Utilitários e helpers
│   └── *.java          # Activities principais
├── res/
│   ├── layout/         # Layouts XML
│   ├── drawable/       # Recursos gráficos
│   └── values/         # Cores, strings e estilos
└── AndroidManifest.xml
```

## 🎯 Objetivos Alcançados

- ✅ Sincronização de dados em tempo real
- ✅ Notificações push personalizadas
- ✅ Integração com APIs externas (Firebase)
- ✅ Gerenciamento de estados online/offline
- ✅ Interface moderna e intuitiva
- ✅ Funcionalidades especiais implementadas (mensagens fixadas e filtro)

## 📄 Licença

Este projeto está licenciado sob a GNU General Public License v3.0 - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 🎓 Contexto Acadêmico

Projeto desenvolvido para a disciplina de **Programação para Dispositivos Móveis** da **Universidade Federal de Uberlândia**, orientado pelo professor **Alexsandro Santos Soares**.

**Valor**: 25 pontos  
**Data de Apresentação**: 01/09/2025

---

*"A Minas Gerais production" 🇧🇷*