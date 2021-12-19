#include <Servo.h>          //디지털 서보 라이브러리
#include <SoftwareSerial.h> //블루투스 모듈 라이브러리
#define BT_RXD 8
#define BT_TXD 7

SoftwareSerial bluetooth(BT_RXD, BT_TXD);  // 블루투스 설정 BTSerial(Tx, Rx)
Servo servo; //디지털 서보

int SP1 = 9; int SP2 = 10; int SP3 = 11; //서보 핀 선언

int nAngle = 100; //디지털 서보 각도 변수

String Scent;     // 블루투스로 입력받는 향 종류 변수 
String Timer;     // 블루투스로 입력받는 타이머 변수
int a;            // Scent를 정수로 변환하여 아두이노 우노에 향 종류를 입력
int Timerint;     // 입력받은 타이머를 정수로 전환 (분)
long Realtime;    // 타이머를 ms단위로 변환

void setup() 
{ 
  //Serial.begin(9600);
  bluetooth.begin(9600);
  pinMode(SP1, OUTPUT);
  pinMode(SP2, OUTPUT);
  pinMode(SP3, OUTPUT);
} 

void loop() 
{ 
  
  if(bluetooth.available()){
    servo.detach();
    Scent = bluetooth.readStringUntil('#');
    Timer = bluetooth.readStringUntil('\n');
    //Serial.print("Scent:");
    //Serial.println(Scent);
    //Serial.print("Timer:");
    //Serial.println(Timer);
    Timerint = Timer.toInt();
    //Serial.print("Timerint:");
    //Serial.println(Timerint);
    Realtime = Timerint * 60000;
    a = Scent.toInt();
    //Serial.print("a:");
    //Serial.println(a);
  }
  if(a == 1){
      servo.attach(SP1);    //1번 모터 작동
      }
  else if (a == 2){
      servo.attach(SP2);    //2번 모터 작동
      }
  else if (a == 3){
      servo.attach(SP3);    //3번 모터 작동
      }    
  func();
}

void func(){
   nAngle = 80;servo.write(nAngle);  //모터를 이용해 헤드를 누름
   delay(300);
   nAngle = 100;servo.write(nAngle); //모터 원위치
   //Serial.print("Realtime:");
   //Serial.println(Realtime);
   delay(Realtime);                  //타이머 작동
  }
