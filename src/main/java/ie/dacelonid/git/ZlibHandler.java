package ie.dacelonid.git;

import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZlibHandler {
    public static byte[] decompress(byte[] compressedData) throws Exception {
        Inflater inflater = new Inflater();
        inflater.setInput(compressedData);

        byte[] output = new byte[1024];
        int decompressedSize = inflater.inflate(output);
        byte[] result = new byte[decompressedSize];
        System.arraycopy(output, 0, result, 0, decompressedSize);
        return result;
    }

    public static byte[] compress(byte[] input) {
        Deflater deflater = new Deflater();
        deflater.setInput(input);
        deflater.finish();

        byte[] output = new byte[1024];
        int compressedSize = deflater.deflate(output);
        byte[] result = new byte[compressedSize];
        System.arraycopy(output, 0, result, 0, compressedSize);
        return result;
    }
}
