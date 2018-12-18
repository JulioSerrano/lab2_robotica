#include <PID_v1.h>
#include <LMotorController.h>
#include "I2Cdev.h"
#include "MPU6050_6Axis_MotionApps20.h"
#include <SoftwareSerial.h>

#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
  #include "Wire.h"
#endif

#define MIN_ABS_SPEED 30

MPU6050 mpu;

int bluetoothTx = 11;
int bluetoothRx = 12;
SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);

// MPU control/status vars
bool dmpReady = false; // set true if DMP init was successful
uint8_t mpuIntStatus; // holds actual interrupt status byte from MPU
uint8_t devStatus; // return status after each device operation (0 = success, !0 = error)
uint16_t packetSize; // expected DMP packet size (default is 42 bytes)
uint16_t fifoCount; // count of all bytes currently in FIFO
uint8_t fifoBuffer[64]; // FIFO storage buffer

// orientation/motion vars
Quaternion q; // [w, x, y, z] quaternion container
VectorFloat gravity; // [x, y, z] gravity vector
float ypr[3]; // [yaw, pitch, roll] yaw/pitch/roll container and gravity vector

//PID
double originalSetpoint = 178;//165 lo mueve
double setpoint = originalSetpoint;
double movingAngleOffset = 0.1;
double input, output, outputSpeed, inputSpeed;

double Kp = 30; // 0 - 100
double Kd = 1.8; // 0 - 200
double Ki = 135; // 0 - 2
PID pid(&input, &output, &setpoint, Kp, Ki, Kd, DIRECT);

double turnChange;
double motorSpeedFactorLeft = 0.60; //lenta
double motorSpeedFactorRight = 0.55;
//MOTOR CONTROLLER
int ENA = 5;
int IN1 = 6;
int IN2 = 7;
int IN3 = 8;
int IN4 = 9;
int ENB = 10;

int i= 0;
unsigned char aux4[2];
boolean flag = true;

LMotorController motorController(ENA, IN1, IN2, ENB, IN3, IN4, motorSpeedFactorLeft, motorSpeedFactorRight);

volatile bool mpuInterrupt = false;

void dmpDataReady() {
  mpuInterrupt = true;
}

void setup() {
  #if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
    Wire.begin();
    TWBR = 24; // 400kHz I2C clock (200kHz if CPU is 8MHz)
  #elif I2CDEV_IMPLEMENTATION == I2CDEV_BUILTIN_FASTWIRE
    Fastwire::setup(400, true);
  #endif

  mpu.initialize();
  Serial.begin(9600);
  bluetooth.begin(9600);
  devStatus = mpu.dmpInitialize();

  mpu.setXGyroOffset(220);
  mpu.setYGyroOffset(76);
  mpu.setZGyroOffset(-85);
  mpu.setZAccelOffset(1688); // 1688 factory default for my test chip

  if (devStatus == 0) {
    mpu.setDMPEnabled(true);
    attachInterrupt(0, dmpDataReady, RISING);
    mpuIntStatus = mpu.getIntStatus();
    dmpReady = true;
    packetSize = mpu.dmpGetFIFOPacketSize();
    //setup PID
    pid.SetMode(AUTOMATIC);
    pid.SetSampleTime(10);
    pid.SetOutputLimits(-255, 255);
  }else {
  // ERROR!
  // 1 = initial memory load failed
  // 2 = DMP configuration updates failed
  Serial.print(F("DMP Initialization failed (code "));
  Serial.print(devStatus);
  Serial.println(F(")"));
  }
}

void forward() {
  double newSetpoint = originalSetpoint - 0.5;
  //pid.setKp(Kp+5);
  //motorController.move(output*1.5,MIN_ABS_SPEED);
  Serial.println(pid.GetKp());
  pid.setpoint(&newSetpoint);
  delay(1000);
  newSetpoint = originalSetpoint + 0.5;
  pid.setpoint(&newSetpoint);
}

void backward() {

}

void turnLeft() {
  turnChange = 0.65; //lenta
  motorSpeedFactorRight = turnChange;
  //motorController.move(output*10, output*5, MIN_ABS_SPEED);
  delay(1000);
  //motorController.move(output*5, output*5, MIN_ABS_SPEED);
  motorSpeedFactorRight = 0.55;
  motorSpeedFactorLeft = 0.55;
}

void turnRight() {
  motorController.move(output*5, output*10, MIN_ABS_SPEED);
}

void loop() {
// if programming failed, don't try to do anything
  if (!dmpReady) return;

  if(bluetooth.available() > 0) {
    //aux3 = bluetooth.read();
    bluetooth.readBytes(aux4,2);
    switch(aux4[0]) {
      case 1:
        Serial.println("Adelante!" + String(aux4[1]));
        forward();
        break;
      case 2:
        Serial.println("Derecha!" + String(aux4[1]));
        motorController.turnRight(159,MIN_ABS_SPEED);
        break;
      case 3:
        Serial.println("Izquierda!" + String(aux4[1]));
        motorController.turnLeft(159,MIN_ABS_SPEED);
        break;
      case 4:
        Serial.println("Atras!" + String(aux4[1]));
        backward();
        break;
      case 5:
        Serial.println("KP!" + String(aux4[1]));
        pid.setKp(aux4[1]);
        Serial.println(pid.GetKp());
        break;
      case 6:
        Serial.println("KI!" + String(aux4[1]));
        pid.setKi(aux4[1]);
        Serial.println(pid.GetKi());
        break;
      case 7:
        Serial.println("KD!" + String(aux4[1]));
        pid.setKd(aux4[1]/100);
        Serial.println(pid.GetKd());
        break;
      case 8:
        Serial.println("New setpoint!" + String(aux4[1]));
        double newSetpoint = aux4[1];
        pid.setpoint(&newSetpoint);
        Serial.println(newSetpoint);
        break;
    }
  }

  while (!mpuInterrupt && fifoCount < packetSize) {
    pid.Compute();
    motorController.move(output,MIN_ABS_SPEED);
  }

  mpuInterrupt = false;
  mpuIntStatus = mpu.getIntStatus();
  fifoCount = mpu.getFIFOCount();

  if ((mpuIntStatus & 0x10) || fifoCount == 1024) {
    mpu.resetFIFO();
    Serial.println(F("FIFO overflow!"));
  }else if (mpuIntStatus & 0x02) {
    while (fifoCount < packetSize) fifoCount = mpu.getFIFOCount();

    mpu.getFIFOBytes(fifoBuffer, packetSize);
    fifoCount -= packetSize;

    mpu.dmpGetQuaternion(&q, fifoBuffer);
    mpu.dmpGetGravity(&gravity, &q);
    mpu.dmpGetYawPitchRoll(ypr, &q, &gravity);
    input = ypr[1] * 180/M_PI + 180;
    inputSpeed = ypr[1] * 180/M_PI + 180;
  }
}
