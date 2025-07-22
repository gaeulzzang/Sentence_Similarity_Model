# 🧠 온디바이스 문서 검색 앱 (Sentence Embedding 기반)

이 프로젝트는 **Snowflake Arctic Embed (sentence)** 모델을 활용해  
사용자의 질의와 가장 유사한 문서 청크를 로컬에서 검색하는 안드로이드 앱입니다.

> 벡터 임베딩부터 질의 검색까지 모든 과정을 **온디바이스**에서 처리합니다.  

<br/>

## 📱 주요 기능

- ✅ JSONL 포맷의 문서 내 문장들을 청크화 및 벡터화
- ✅ ONNX 모델 기반 문장 임베딩 수행
- ✅ 코사인 유사도를 기반으로 질의와 가장 유사한 청크 Top-N 반환
- ✅ 모든 처리는 **로컬 디바이스에서 비동기 처리**

<br/>

## 🔧 기술 스택

| 영역           | 사용 기술 |
|----------------|-----------|
| UI             | Jetpack Compose |
| 모델 임베딩    | [snowflake-arctic-embed-s](https://huggingface.co/Snowflake/snowflake-arctic-embed-s/tree/main/onnx) |
| 모델 포맷      | ONNX |
| 임베딩 라이브러리 | [shubham0204/Sentence-Embeddings-Android](https://github.com/shubham0204/Sentence-Embeddings-Android) |
| 상태관리       | ViewModel + StateFlow |
| 비동기 처리     | Kotlin Coroutine |

<br/>

## 📦 APK 다운로드

[👉 최신 버전 APK 다운로드](https://drive.google.com/file/d/1xEmY7nBKE6k71egBAMU8Xsx9qw_VUZlk/view?usp=sharing)

또는 Android Studio에서 직접 빌드해 테스트할 수 있습니다.

<br/>

## 🚀 Android Studio 실행 방법

### 1. 레포지토리 클론

```bash
git clone https://github.com/gaeulzzang/Sentence_Similarity_Model.git
```

### 2. Android Studio로 열기
최소 SDK: 24 (Nougat)
권장 SDK: 33 이상

### 3. 모델과 토크나이저는 앱 내부에 이미 포함되어 있음
assets/model.onnx, assets/tokenizer.json 파일이 있어 별도 설정이 필요 없습니다.

<br/>




