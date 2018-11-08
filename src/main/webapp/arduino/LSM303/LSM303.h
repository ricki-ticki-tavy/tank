#include <stdint.h>
  #define LSM303_ACL_ADDR 0x1D
//  #define LSM303_MAG_ADDR 0x1E;

  #define  LSM303_NIT_SAMPLES 30


  #define  LSM303_ACL_CTRL_REG1_A 0x20
  #define  LSM303_ACL_CTRL_REG2_A 0x21
  #define  LSM303_ACL_CTRL_REG4_A 0x23
  #define  LSM303_ACL_FIFO_CTRL 0x2E
  #define  LSM303_ACL_CTRL_REG1_A_INIT_VAL 0x2F
  #define  LSM303_ACL_CTRL_REG2_A_INIT_VAL 0x0C
  #define  LSM303_ACL_CTRL_REG4_A_INIT_VAL 0xC4
  #define  LSM303_ACL_FIFO_CTRL_INIT_VAL 0x41
  #define  LSM303_ACL_REFX_LOW 0x3A
  #define  LSM303_ACL_REFY_LOW 0x3C
  #define  LSM303_ACL_REFZ_LOW 0x3E
  #define  LSM303_ACL_OUT_X_L_A 0x28

  #define  LSM303_ACL_REFX_LOW_INIT 0x3A
  #define  LSM303_ACL_REFY_LOW_INIT 0x3C
  #define  LSM303_ACL_REFZ_LOW_INIT 0x3E
  #define  LSM303_REQ_TIMEOUT_MS 30


  #define  LSM303_CTRL_REG1_M 0x20
  #define  LSM303_TEMP_ENABLED 0x80
  #define  LSM303_OM_UH 0x60
  #define  LSM303_ODR_20HZ 0x14

  #define  LSM303_CTRL_REG2_M 0x21
  #define  LSM303_CTRL_REG3_M 0x22
  #define  LSM303_CTRL_REG4_M 0x23
  #define  LSM303_OMZ_UH 0x0C
  #define  LSM303_CTRL_REG5_Mx24
  #define  LSM303_BDU 0x80
  #define  LSM303_MAG_OUT_X_L_A 0x28


class LSM303 {
  private:
    uint8_t sendPacket(uint8_t addr, uint8_t reg, uint8_t data);
    int16_t twoBytesToInt(uint8_t low, uint8_t high);
  public:
    int16_t acclAxys[3];
    double krengen;
    double tangage;
    LSM303(void);
    uint8_t init(void);
    uint8_t readCurrentAcclAxys();
};
