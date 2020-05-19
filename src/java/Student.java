
public class Student implements User{
    
    private String name, surname, id;
    
    public Student(String name, String surname, String id){
        this.name = name;
        this.surname = surname;
        this.id = id;
    }
    
    @Override
    public String getName(){
        return name;
    }
    
    @Override
    public String getId(){
        return id;
    }
    
    @Override
    public String getSurname(){
        return surname;
    }
}