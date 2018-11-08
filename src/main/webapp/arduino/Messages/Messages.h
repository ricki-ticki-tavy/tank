
class MESSAGES {
  public:
    MESSAGES(void);
    void  printConstMessage(const char data[], bool newLine);
    void  printByteAsHEX(unsigned char data);
};

extern MESSAGES Messages;
