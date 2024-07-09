import com.gec.enums.Role;

public class test1 {
    public static void main(String[] args) {
        Role role = Role.getEnum("1");
        System.out.println(role);
    }
}
