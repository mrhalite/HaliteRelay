# HaliteRelay v1.4

## Change Log
v1.1
    - Release

v1.2
    - 연락처에서 회사와 직책을 찾아 보여주도록 수정
    - Layout 오류 수정
    
v1.3
    - MMS 받기 기능 추가
    - cmdcmd 기능 예외 처리 추가
    
v1.4
    - 카톡 알림 받기 오류 수정
    
-----

HaliteRelay는 안드로이드 폰에 설치해 (1)문자를 받거나 (2)전화가 걸려오거나 (3)카카오톡이 오면 받았다는 내용을 설정해 놓은 전화 번호로 문자를 보내주는 프로그램입니다.

1. 설치
    - 출처를 알 수 없는 앱을 임시로 켜서 설치합니다.
    - 설치 후 어플리케이셔 관리의 HaliteRelay 앱을 선택하고 권한 설정에서 SMS, 전화, 주소록 권한을 켜줍니다.
1. 설정
    - 앱을 실행 후 전달 받을 전화 번호르 입력하고 원하는 기능을 선택해 (현재 E-Mail은 동작하지 않음) 저장해야 정상 동작합니다.
1. 동작
    - 문자나 전화는 받은 전화 번호를 주소록에서 찾아 이름, 회사, 직책을 같이 보냅니다.
    - 카카오톡은 Notification을 확인해 Notification의 title, text를 보내므로 Notification에서 보여지는 보내 사람, 내용이 전달됩니다.
1. 그외 기능
    - 반대로 HaliteRelay가 설치된 핸드폰으로 문자를 보내되 'cmdcmd_전화번호_내용'과 같이 보내면 '전화번호'로 '내용'을 보내줍니다. (cmdcmd는 소문자로)
