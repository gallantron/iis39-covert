package at.tugraz.iis.group39.app1.at.tugraz.iis.group39.app1.testing;

public class DummyChannelPipe
{
    private final DummyOutputChannel _output;
    public DummyOutputChannel entrance() { return _output; }

    private final DummyInputChannel _input;
    public DummyInputChannel exit() { return _input; }

    private DummyChannelPipe(DummyOutputChannel out, DummyInputChannel in) { _output = out; _input = in; }

    public static DummyChannelPipe make()
    {
        DummyInputChannel exit = new DummyInputChannel();
        DummyOutputChannel entrance = new DummyOutputChannel(exit);
        return new DummyChannelPipe(entrance, exit);
    }
}
