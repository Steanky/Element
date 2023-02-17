import com.github.steanky.element.core.annotation.*;

@Model("test.other")
public class OtherElement {
    @FactoryMethod
    public OtherElement(Data data) {

    }

    @DataObject
    public record Data(String data) {

    }
}