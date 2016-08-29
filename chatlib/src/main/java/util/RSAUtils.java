package util;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

public class RSAUtils {
    private KeyPair keyPair ;   //密匙对
    public RSAUtils(){
        try{
            this.keyPair = this.getKeyPair();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 生成密钥对
     * @return KeyPair 初始化的密匙对返回
     * @throws Exception
     */
    private KeyPair getKeyPair() throws Exception{
        try {
            KeyPairGenerator keyPainGen = KeyPairGenerator.getInstance("RSA",new org.bouncycastle.jce.provider.BouncyCastleProvider());
            //这个值关系到块加密的大小，可以更改，但是不要太大，否则效率会低
            final int KEY_SIZE = 1024 ;
            keyPainGen.initialize(KEY_SIZE,new SecureRandom());
            KeyPair keyPair = keyPainGen.genKeyPair();
            return keyPair;
        }catch(Exception e){
            throw new Exception(e.getMessage());
        }
    }
    /**
     * 生成公钥
     * @param modulus
     * @param publicExponent
     * @return RSAPublicKey
     * @throws Exception
     */
    private RSAPublicKey generateRSAPublicKey(byte[] modulus, byte[] publicExponent) throws Exception {

        KeyFactory keyFac = null;
        try {
            keyFac = KeyFactory.getInstance("RSA", new org.bouncycastle.jce.provider.BouncyCastleProvider());
        } catch (NoSuchAlgorithmException ex) {
            throw new Exception(ex.getMessage());
        }
        RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(publicExponent));
        try {
            return (RSAPublicKey) keyFac.generatePublic(pubKeySpec);
        } catch (InvalidKeySpecException ex) {
            throw new Exception(ex.getMessage());
        }

    }

    /**
     * 生成私钥
     * @param modulus
     * @param privateExponent
     * @return RSAPrivateKey
     * @throws Exception
     */
    private RSAPrivateKey generateRSAPrivateKey(byte[] modulus, byte[] privateExponent) throws Exception {
        KeyFactory keyFac = null;
        try {
            keyFac = KeyFactory.getInstance("RSA", new org.bouncycastle.jce.provider.BouncyCastleProvider());
        } catch (NoSuchAlgorithmException ex) {
            throw new Exception(ex.getMessage());
        }
        RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(new BigInteger(modulus), new BigInteger(privateExponent));
        try {
            return (RSAPrivateKey) keyFac.generatePrivate(priKeySpec);
        } catch (InvalidKeySpecException ex) {
            throw new Exception(ex.getMessage());
        }
    }

    /**
     * 加密
     * @param key 加密的密钥
     * @param data 待加密的明文数据
     * @return 加密后的数据
     * @throws Exception
     */
    public byte[] encrypt(Key key, byte[] data) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("RSA", new org.bouncycastle.jce.provider.BouncyCastleProvider());
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //获得加密块大小，如:加密前数据为128个byte，而key_size=1024 加密块大小为127 byte,加密后为128个byte;
            //因此共有2个加密块，第一个127 byte第二个为1个byte
            int blockSize = cipher.getBlockSize();
            int outputSize = cipher.getOutputSize(data.length);//获得加密块加密后块大小
            int leavedSize = data.length % blockSize;
            int blocksSize = leavedSize != 0 ? data.length / blockSize + 1 : data.length / blockSize;
            byte[] raw = new byte[outputSize * blocksSize];
            int i = 0;
            while (data.length - i * blockSize > 0) {
                if (data.length - i * blockSize > blockSize)
                    cipher.doFinal(data, i * blockSize, blockSize, raw, i * outputSize);
                else
                    cipher.doFinal(data, i * blockSize, data.length - i * blockSize, raw, i * outputSize);
                //这里面doUpdate方法不可用，查看源代码后发现每次doUpdate后并没有什么实际动作除了把byte[]放到ByteArrayOutputStream中
                //，而最后doFinal的时候才将所有的byte[]进行加密，可是到了此时加密块大小很可能已经超出了OutputSize所以只好用dofinal方法。
                i++;
            }
            return raw;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 解密
     * @param key 解密的密钥
     * @param raw 已经加密的数据
     * @return 解密后的明文
     * @throws Exception
     */
    public byte[] decrypt(Key key, byte[] raw) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("RSA", new org.bouncycastle.jce.provider.BouncyCastleProvider());
            cipher.init(cipher.DECRYPT_MODE, key);
            int blockSize = cipher.getBlockSize();
            ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
            int j = 0;
            while (raw.length - j * blockSize > 0) {
                bout.write(cipher.doFinal(raw, j * blockSize, blockSize));
                j++;
            }
            return bout.toByteArray();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 返回公钥
     * @return
     * @throws Exception
     */
    public RSAPublicKey getRSAPublicKey() throws Exception{

        //获取公钥
        RSAPublicKey pubKey = (RSAPublicKey) keyPair.getPublic();
        //获取公钥系数(字节数组形式)
        byte[] pubModBytes = pubKey.getModulus().toByteArray();
        //返回公钥公用指数(字节数组形式)
        byte[] pubPubExpBytes = pubKey.getPublicExponent().toByteArray();
        //生成公钥
        RSAPublicKey recoveryPubKey = this.generateRSAPublicKey(pubModBytes,pubPubExpBytes);
        return recoveryPubKey;
    }

    /**
     * 获取私钥
     * @return
     * @throws Exception
     */
    public RSAPrivateKey getRSAPrivateKey() throws Exception{

        //获取私钥
        RSAPrivateKey priKey = (RSAPrivateKey) keyPair.getPrivate();
        //返回私钥系数(字节数组形式)
        byte[] priModBytes = priKey.getModulus().toByteArray();
        //返回私钥专用指数(字节数组形式)
        byte[] priPriExpBytes = priKey.getPrivateExponent().toByteArray();
        //生成私钥
        RSAPrivateKey recoveryPriKey = this.generateRSAPrivateKey(priModBytes,priPriExpBytes);
        return recoveryPriKey;
    }
}