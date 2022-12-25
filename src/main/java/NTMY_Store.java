import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class NTMY_Store {
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

    public NTMY_Store() {
        generateKeyPair();
    }

    public void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            //Elliptic Curve Digital Signature Algorithm - thuật toán sinh chữ ký số dựa trên đường cong Elliptic
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG"); //Tạo ra mã ngẫu nhiên
            //Tạo một đặc tả tham số để tạo các tham số miền đường cong Elliptic
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            // Khởi tạo bộ tạo khóa và sinh một KeyPair
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            // Thiết lập khóa bảo mật và khóa công khai cho Cặp khóa
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: NTMY_Blockchain.UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();

            if(UTXO.isMine(publicKey)) {
                UTXOs.put(UTXO.id,UTXO);
                total += UTXO.value ;
            }
        }
        return total;
    }

    public Transaction sendFunds(PublicKey _recipient,float value ) {
        if(getBalance() < value) {
            System.out.println("#Số lượng VNPT-Net Router trong kho không đủ, vui lòng nhập lại số lượng!");
            return null;
        }
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if(total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
        newTransaction.generateSignature(privateKey);

        for(TransactionInput input: inputs){
            UTXOs.remove(input.transactionOutputId);
        }

        return newTransaction;
    }
}