#define PCA9685_ADDRESS 0x40

class PCA9685 {
  private:
    int readTwoBytes(void);
    void writeTwoBytes(int data);
  public:
    PCA9685(void);
    
    unsigned char init(void);
    unsigned char setChannelValue(unsigned char channel, int value);
    unsigned char setPwmFrequency(int frequency);
};

extern PCA9685 Pca9685;