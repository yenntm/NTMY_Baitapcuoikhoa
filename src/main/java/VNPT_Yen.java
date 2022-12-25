import java.util.ArrayList;
import java.util.Date;
public class VNPT_Yen {
    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); //Khai báo mảng chứa các giao dịch.
    public long timeStamp;
    public int nonce;

    // Hàm tạo khối
    public VNPT_Yen(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }
    //Tính toán mã băm mới dựa vào nội dung khối
    public String calculateHash() {
        String calculatedhash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) + merkleRoot
        );
        return calculatedhash;
    }

    //Tăng giá trị của Nonce cho đến khi đạt được mã băm mục tiêu
    public void mineVNPT_Yen(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = new String(new char[difficulty]).replace('\0','0'); //Khởi tạo chuỗi với độ khó  là "0"
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!!: " + hash);
    }

    //Thêm giao dịch vào khối
    public boolean addTransaction(Transaction transaction) {
        //xử lý giao dịch và kiểm tra xem có hợp lệ không; Nếu là khối là khối gốc thì không cần kiểm tra
        if(transaction == null) return false;
        if((!"0".equals(previousHash))) {
            if((transaction.processTransaction() != true)) {
                System.out.println("Giao dịch không xử lý được!!!");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Cập nhật thành công!!!");
        return true;
    }
}