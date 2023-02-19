import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.annotation.document.*;

@Model("test.other")
@Description("""
        This is a test element for testing purposes.
        """)
public class OtherElement {
    @FactoryMethod
    public OtherElement(Data data) {

    }

    @DataObject
    public record Data(String data) {

    }
}