package fr.esiea.pst.abracadabra;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.jtransforms.fft.FloatFFT_1D;

public class FftProcessor {

  public void fft(File audioFile) throws UnsupportedAudioFileException, IOException {
    byte bytes[] = new byte[4096];

    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
    AudioFormat audioFormat = audioInputStream.getFormat();
    long dataLength = audioInputStream.getFrameLength() * audioFormat.getFrameSize();
    System.out.println("dataLength : "+dataLength);

    int nbView = (int) dataLength / bytes.length;
    System.out.println("nbView : " + nbView);
    System.out.println("frequences Ranges \t 20-250 Hz\t\t250-2000 Hz\t\t2000-6000 Hz\t\t6000-22050");
    float data[][] = new float[nbView][bytes.length * 2];
    double Magnitudes[] = new double[bytes.length/2];
    FloatFFT_1D fft = new FloatFFT_1D(bytes.length);

    try (BufferedWriter fw1 = new BufferedWriter(new FileWriter("data.txt"));
         BufferedWriter fw2 = new BufferedWriter(new FileWriter("fft.txt"))) {
      for (int i = 0; i < nbView; i++) {
        int read = readSlidingWindow(audioInputStream, bytes);

        for (int j = 0, k=0; j < read; j++, k=k+2) {
            data[i][k] = bytes[j];
          fw1.write(bytes[j]);
          fw1.newLine();
        }
          
        fft.complexForward(data[i]);
    //    System.out.println(fft);

        for (int j = 0,k=0; j < data[i].length/2; k++, j++) {
          fw2.append(Float.toString(data[i][j])).append("\t\t");
          j++;
          fw2.append(Float.toString(data[i][j]));
          if(k<Magnitudes.length){
        	  Magnitudes[k] = Math.sqrt(Math.pow(data[i][j-1],2)+Math.pow(data[i][j],2));
        	  fw2.write("\t\t" + k*44100/4096 + " Hz\t\t Magn :" + Magnitudes[k]);
          }
          fw2.newLine();
        }
        System.out.print("Freq for Max magnitude : ");
        System.out.print(GetMaxFreq(Magnitudes, 2, 24) + " Hz ("+ GetMaxMagn(Magnitudes, 2, 24) + ")   \t");
        System.out.print(GetMaxFreq(Magnitudes, 25, 187) + " Hz ("+ GetMaxMagn(Magnitudes, 25, 187) + ")  \t");
        System.out.print(GetMaxFreq(Magnitudes, 188, 558) + " Hz ("+ GetMaxMagn(Magnitudes, 188, 558) + ") \t");
        System.out.print(GetMaxFreq(Magnitudes, 559, 2048) + " Hz ("+ GetMaxMagn(Magnitudes, 559, 2048) + ") \t");
        System.out.print("\n");
      }
    }
    //autoclose
  }

  private int readWindow(InputStream inputStream, byte[] bytes) throws IOException {
    int read = 0;
    while (read < bytes.length) {
      int r = inputStream.read(bytes, read, bytes.length - read);
      if (r != -1) {
        read += r;
      }
      else {
        break;
      }
    }
    return read;
  }
  
  private int readSlidingWindow(InputStream inputStream, byte[] bytes) throws IOException {
	    if(inputStream.markSupported()) {
	      int skipValue = bytes.length/2; //50%
	      inputStream.mark(skipValue);
	      int read = readWindow(inputStream, bytes);
	      inputStream.reset();
	      inputStream.skip(skipValue);
	      return read;
	    }
	    else {
	      return readWindow(inputStream, bytes);
	    }
	  }
  
  private int GetMaxFreq(double[] magnitudes, int RangeLow, int RangeHigh){
	  int Freq = 0;
	  int Magnitude = 0;
	  
	  for(int i=RangeLow; i<RangeHigh; i++){
		  if(magnitudes[i]>Magnitude){
			Magnitude = (int) magnitudes[i];
		  	Freq = i*44100/4096;
		  }
	  }
	  
	  return Freq;
  }
  
  private float GetMaxMagn(double[] magnitudes, int RangeLow, int RangeHigh){
	  int Magnitude = 0;
	  
	  for(int i=RangeLow; i<RangeHigh; i++){
		  if(magnitudes[i]>Magnitude){
			Magnitude = (int) magnitudes[i];
		  }
	  }
	  
	  return Magnitude;
  }
}
