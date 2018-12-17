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

#define  Left_Enoder     0x01
#define  Right_Enoder    0x02
#define  Left_PWM        0x03
#define  Right_PWM       0x04
#define  Balance_Angle   0x05
#define  Upright_Kp      0x06
#define  Upright_Ki      0x07
#define  Upright_Kd      0x08
#define  Speed_Kp        0x09
#define  Speed_Ki        0x10
#define  Speed_Kd        0x0A
#define  Rotate_Kp       0x0B
#define  Rotate_Ki       0x0C
#define  Rotate_Kd       0x0D
#define  Contrl_val      0x0E
#define  angle_output    0x0F
#define  speed_output    0x11

unsigned char a[12]={0xAA,0xAA,0xAA,0xAA,0x00,0x00,0x00,0x00,0xFF,0xFF,0xFF,0xFF};
unsigned char RxBuf[12]={0};

//PID
double originalSetpoint = 178;//165 lo mueve
double setpoint = originalSetpoint;
double movingAngleOffset = 0.1;
double input, output, outputSpeed, inputSpeed;

//adjust these values to fit your own design
double Kp = 30; // 0 - 100   
double Kd = 1.8; // 0 - 200
double Ki = 135; // 0 - 2
PID pid(&input, &output, &setpoint, Kp, Ki, Kd, DIRECT);
double KpSpeed = 45; // 0 - 100   
double KdSpeed = 1.8; // 0 - 200
double KiSpeed = 135; // 0 - 2
PID pidSpeed(&inputSpeed, &outputSpeed, &setpoint, KpSpeed, KiSpeed, KdSpeed, DIRECT);

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

volatile bool mpuInterrupt = false; // indicates whether MPU interrupt pin has gone high
void dmpDataReady()
{
mpuInterrupt = true;
}


void setup()
{
// join I2C bus (I2Cdev library doesn't do this automatically)
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

// supply your own gyro offsets here, scaled for min sensitivity
mpu.setXGyroOffset(220);
mpu.setYGyroOffset(76);
mpu.setZGyroOffset(-85);
mpu.setZAccelOffset(1688); // 1688 factory default for my test chip

// make sure it worked (returns 0 if so)
if (devStatus == 0)
{
// turn on the DMP, now that it's ready
mpu.setDMPEnabled(true);

// enable Arduino interrupt detection
attachInterrupt(0, dmpDataReady, RISING);
mpuIntStatus = mpu.getIntStatus();

// set our DMP Ready flag so the main loop() function knows it's okay to use it
dmpReady = true;

// get expected DMP packet size for later comparison
packetSize = mpu.dmpGetFIFOPacketSize();

//setup PID
pid.SetMode(AUTOMATIC);
pid.SetSampleTime(10);
pid.SetOutputLimits(-255, 255); 
}
else
{
// ERROR!
// 1 = initial memory load failed
// 2 = DMP configuration updates failed
// (if it's going to break, usually the code will be 1)
Serial.print(F("DMP Initialization failed (code "));
Serial.print(devStatus);
Serial.println(F(")"));
}
}
void forward()
{
  double newSetpoint = originalSetpoint - 0.5;
  //pid.setKp(Kp+5);
  //motorController.move(output*1.5,MIN_ABS_SPEED);
  Serial.println(pid.GetKp());
  pid.setpoint(&newSetpoint);
  delay(1000);
  newSetpoint = originalSetpoint + 0.5;
  pid.setpoint(&newSetpoint);
}
void backward()
{
  
}

void turnLeft()
{
  turnChange = 0.65; //lenta
  motorSpeedFactorRight = turnChange;
  //motorController.move(output*10, output*5, MIN_ABS_SPEED);
  delay(1000);
  //motorController.move(output*5, output*5, MIN_ABS_SPEED);
  motorSpeedFactorRight = 0.55;
  motorSpeedFactorLeft = 0.55;
}
void turnRight()
{
  motorController.move(output*5, output*10, MIN_ABS_SPEED);
}

void loop()
{
// if programming failed, don't try to do anything
if (!dmpReady) return;

// wait for MPU interrupt or extra packet(s) available
// wait for MPU interrupt or extra packet(s) available
  if(bluetooth.available() > 0){
    //aux3 = bluetooth.read();
    bluetooth.readBytes(aux4,2);
    switch(aux4[0])
    {
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


while (!mpuInterrupt && fifoCount < packetSize)
{
//no mpu data - performing PID calculations and output to motors 
        pid.Compute();
        motorController.move(output,MIN_ABS_SPEED);
        /*if(output < 25 && output > 0 ){ // cuando se encuentra estable(vertical) aumenta la velocidad de la ruedas
          //Serial.println(output);
          motorController.move(output*10,MIN_ABS_SPEED);
        }

        else{
           motorController.move(output,MIN_ABS_SPEED);
        }*/
   
}

// reset interrupt flag and get INT_STATUS byte
mpuInterrupt = false;
mpuIntStatus = mpu.getIntStatus();

// get current FIFO count
fifoCount = mpu.getFIFOCount();

// check for overflow (this should never happen unless our code is too inefficient)
if ((mpuIntStatus & 0x10) || fifoCount == 1024)
{
// reset so we can continue cleanly
mpu.resetFIFO();
Serial.println(F("FIFO overflow!"));

// otherwise, check for DMP data ready interrupt (this should happen frequently)
}
else if (mpuIntStatus & 0x02)
{
// wait for correct available data length, should be a VERY short wait
while (fifoCount < packetSize) fifoCount = mpu.getFIFOCount();

// read a packet from FIFO
mpu.getFIFOBytes(fifoBuffer, packetSize);

// track FIFO count here in case there is > 1 packet available
// (this lets us immediately read more without waiting for an interrupt)
fifoCount -= packetSize;

mpu.dmpGetQuaternion(&q, fifoBuffer);
mpu.dmpGetGravity(&gravity, &q);
mpu.dmpGetYawPitchRoll(ypr, &q, &gravity);
input = ypr[1] * 180/M_PI + 180;
inputSpeed = ypr[1] * 180/M_PI + 180;
}
}
