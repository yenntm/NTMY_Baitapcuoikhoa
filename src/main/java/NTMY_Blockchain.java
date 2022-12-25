import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
public class NTMY_Blockchain {
    public static ArrayList<VNPT_Yen> blockchain = new ArrayList<VNPT_Yen>();
    public static int difficulty = 5;
    public static float minimumTransaction = 0.1f;
    public static NTMY_Store NTMY_Kho; //Kho
    public static NTMY_Store NTMY_Cuahang; //Cửa hàng
    public static Transaction genesisTransaction;
    public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
    public static void main(String[] args) {
        //Thêm khối vào chuỗi khối
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Thiết lập bảo mật bằng phương thức BouncyCastleProvider
        Scanner sc = new Scanner(System.in);
        //Tạo ra các store
        NTMY_Kho = new NTMY_Store();
        NTMY_Cuahang = new NTMY_Store();
        NTMY_Store coinbase = new NTMY_Store();
        Transaction sendFund;
        System.out.print("-Nhập số lượng VNPT-Net Router đang tồn trong kho : ");
        float initBalanceA = sc.nextFloat();
        //Khởi tạo giao dịch gốc, để nhập số lượng VNPT-Net Router có trong kho NVH_Kho
        genesisTransaction = new Transaction(coinbase.publicKey, NTMY_Kho.publicKey, initBalanceA, null);
        genesisTransaction.generateSignature(coinbase.privateKey); //Gán private key (ký thủ công) vào giao dịch gốc
        genesisTransaction.transactionId = "0"; //Gán ID cho giao dịch gốc
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); //Thêm Transactions Output
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //Lưu giao dịch đầu tiên vào danh sách UTXOs
        VNPT_Yen genesis = new VNPT_Yen("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);
        System.out.println("-Số lượng VNPT-Net Router trong kho là : " + NTMY_Kho.getBalance());
        System.out.print("-Nhập số lượng VNPT-Net Router đang còn tại Cửa hàng : ");
        float initBalanceB = sc.nextFloat();
        //Khởi tạo giao dịch gốc, để nhập số lượng VNPT-Net Router còn tại cửa hàng kho NVH_Cuahang
        genesisTransaction = new Transaction(coinbase.publicKey, NTMY_Cuahang.publicKey, initBalanceB, null);
        genesisTransaction.generateSignature(coinbase.privateKey); //Gán private key (ký thủ công) vào giao dịch gốc
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); //Thêm Transactions Output
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //Lưu giao dịch đầu tiên vào danh sách UTXOs
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);
        System.out.println("-Số lượng VNPT-Net Router đang có tại Cửa hàng là : " + NTMY_Cuahang.getBalance());
        VNPT_Yen block1 = new VNPT_Yen(genesis.hash);
        boolean fail = true;
        while (fail){
            System.out.print("-Nhập số lượng VNPT-Net Router cần chuyển từ kho sang cửa hàng : ");
            float numberTransfer = sc.nextFloat();
            System.out.println("Đang xử lý ........................");
            sendFund = NTMY_Kho.sendFunds(NTMY_Cuahang.publicKey, numberTransfer);
            if (sendFund==null){
                continue;
            }else{
                fail= false;
                block1.addTransaction(sendFund);
            }
        }
        addBlock(block1);
        System.out.println("Số lượng VNPT-Net Router mới trong kho và cửa hàng sau khi chuyển: ");
        System.out.println("-Số lượng VNPT-Net Router trong kho hiện tại là : " + NTMY_Kho.getBalance());
        System.out.println("-Số lượng VNPT-Net Router của Cửa hàng hiện tại là : " + NTMY_Cuahang.getBalance());
    }
    public static Boolean isChainValid() {
        VNPT_Yen currentBlock;
        VNPT_Yen previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //Tạo một danh sách hoạt động tạm thời của các giao dịch chưa được thực thi tại một trạng thái khối nhất định.
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        //Duyệt chuỗi khối để kiểm tra các mã băm:
        for(int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //Kiểm tra, so sánh mã băm đã đăng ký với mã băm được tính toán
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("#Mã băm khối hiện tại không khớp");
                return false;
            }
            //So sánh mã băm của khối trước với mã băm của khối trước đã được đăng ký
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("#Mã băm khối trước không khớp");
                return false;
            }
            //Kiểm tra xem mã băm có lỗi không
            if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("#Khối này không đào được do lỗi!");
                return false;
            }
            //Vòng lặp kiểm tra các giao dịch
            TransactionOutput tempOutput;
            for(int t=0; t <currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if(!currentTransaction.verifySignature()) {
                    System.out.println("#Chữ ký số của giao dịch (" + t + ") không hợp lệ");
                    return false;
                }
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#Các đầu vào không khớp với đầu ra trong giao dịch (" + t + ")");
                    return false;
                }
                for(TransactionInput input: currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
                        System.out.println("#Các đầu vào tham chiếu trong giao dịch (" + t + ") bị thiếu!");
                        return false;
                    }
                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#Các đầu vào tham chiếu trong giao dịch (" + t + ") có giá trị không hợp lệ");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }
                for(TransactionOutput output: currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }
                if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
                    System.out.println("#Giao dịch(" + t + ") có người nhận không đúng!");
                    return false;
                }
                if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
                    System.out.println("#Đầu ra của giao (" + t + ") không đúng với người gửi.");
                    return false;
                }
            }
        }
        System.out.println("Chuỗi khối hợp lệ!");
        return true;
    }

    public static void addBlock(VNPT_Yen newBlock) {
        newBlock.mineVNPT_Yen(difficulty);
        blockchain.add(newBlock);
    }
}