import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StegenoEncoder {

	// PNG file Magic Number
	private static final BitSequence PNG_MN = new BitSequence(
			"1000100101010000010011100100011100001101000010100001101000001010");

	private static final BitSequence MAGIC_NUMBER = new BitSequence("010100110100111001001111");

	public static void main(String[] args) throws IOException, LengthException {

		StegenoEncoder steno = new StegenoEncoder();

		if (args[0].equals("-e")) {
			List<BitSequence> imageBits = steno.reader(args[1]);
			List<BitSequence> textBits = steno.reader(args[2]);

			List<BitSequence> toEncode = steno.prepareText(textBits);
			
			// Current max that the program can handle without breaking the 
			// PNG file
			if( textBits.size() > 1279 ) {
				System.err.println("The file \"" + args[2] + "\" is too large.");
				System.exit(1);
			}
			
			System.out.println("Image Size:      " + imageBits.size());
			System.out.println("Text Size:       " + textBits.size());
			System.out.println("Allocated space: " + toEncode.size());

			steno.encode(toEncode, imageBits, PNG_MN.length() / 8);
			
			String fileName = args[2].substring(0, args[2].indexOf('.'));
			
			
			steno.writeImage(imageBits, "encoded_" + fileName + ".png");
		} else if (args[0].equals("-d")) {
			List<BitSequence> imageBits = steno.reader(args[1]);
			BitSequence bits = new BitSequence();

			int counter = 0;
			for (BitSequence b : imageBits) {
				
				bits.appendBit(b.getBit(6));
				bits.appendBit(b.getBit(7));
				
				if (bits.length() == 8) {
					char c = (char) bits.toByte();
					bits = new BitSequence();
					// Reached the end of the string.
					if( c == '\0') {
						return;
					}
					
					if( counter > 2) {
						System.out.print(c);
					} else {
						counter++;
					}
				}
			}
		} 
	}

	private void writeImage(List<BitSequence> imageBits, String outFile) {
		byte data[] = new byte[imageBits.size()];
		System.out.println("Writing " + data.length + " bytes...");
		for (int i = 0; i < imageBits.size() - 1; i++) {
			try {
				data[i] = imageBits.get(i).toByte();
			} catch (LengthException e) {
				e.printStackTrace();
			}
		}
		FileOutputStream out;
		try {
			out = new FileOutputStream(outFile);
			out.write(data);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<BitSequence> prepareText(List<BitSequence> textBits) {
		ArrayList<BitSequence> retList = new ArrayList<>();
		for (int i = 0; i < MAGIC_NUMBER.length(); i += 2) {
			String s = (MAGIC_NUMBER.getBit(i)) + "";
			s += (MAGIC_NUMBER.getBit(i + 1));
			retList.add(new BitSequence(s));
		}

		for (BitSequence b : textBits) {
			String str = b.toString();
			for (int i = 0; i < str.length(); i += 2) {
				String s = (str.charAt(i)) + "";
				s += (str.charAt(i + 1));
				retList.add(new BitSequence(s));
			}
		}
		
		for (int i = 0; i < 8; i ++) {
			retList.add(new BitSequence("00"));
		}

		return retList;
	}

	
	private void encode(List<BitSequence> toEncode, List<BitSequence> imageBits, int offset) {
		System.out.println("Starting at offset " + offset);
		BitSequence build = new BitSequence();
		for (int i = (offset + 1); i < (offset + toEncode.size() ); i++) {
			if( i < toEncode.size() ) {
				BitSequence bse = toEncode.get(i);
				BitSequence bsc = imageBits.get(i);
				int a = bse.getBit(0);
				int b = bse.getBit(1);
				build.appendBit(a);
				build.appendBit(b);
				bsc.setBit(bsc.length() - 2, bse.getBit(0));
				bsc.setBit(bsc.length() - 1, bse.getBit(1));
			}
		}
	}

	/**
	 * Turns any file into a list of BitSequences, 8 bits long, representing 
	 * each byte.
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private List<BitSequence> reader(String fileName) throws IOException {
		File file = new File(fileName);
		FileInputStream fis = new FileInputStream(file);
		byte[] bytes = new byte[(int) file.length()];
		fis.read(bytes);
		fis.close();
		ArrayList<BitSequence> bsal = new ArrayList<>();
		for (byte b : bytes) {
			bsal.add(new BitSequence(b));
		}
		return bsal;
	}

}
