// Cipher Modes of operation:
#define MODE_ECB 0  // Electronic Codebook
#define MODE_CBC 1  // Cipher Block Chaining

class CCryptoGear
{
private:
	static const unsigned char m_InitializationVector = 0; // 0-255
	static const unsigned short m_lenKeystream = 256;
	unsigned char m_ModeOfOperation;
	unsigned char m_KeyStream[m_lenKeystream];

public:
	CCryptoGear(unsigned char* pKey, 
		unsigned int lenKey, 
		unsigned char ModeOfOperation = MODE_CBC,
		unsigned char InitializationVector = m_InitializationVector);

	CCryptoGear();

	void Initialize(unsigned char* pKey, unsigned int lenKey, 
		unsigned char ModeOfOperation = MODE_CBC,
		unsigned char InitializationVector = m_InitializationVector);

	void Encrypt(unsigned char pData[], unsigned long lenData);
	void Decrypt(unsigned char pData[], unsigned long lenData);
}; 
