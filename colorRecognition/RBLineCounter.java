package colorRecognition;

public class RBLineCounter {
	public static void main(String[] args) throws InterruptedException {
		ColorLineRecognition reccog = new ColorLineRecognition(0,false);
		int redLines = 0, blueLines = 0;
		int[] SV = {-1,120,-1,-1};
		int[] HL = {70,-1,-1};
		try {
			reccog.createGui(1200,800);
			//reccog.cropImage(reccog.CAM_WIDTH/3,300,(reccog.CAM_WIDTH*2/3-80),100);
			for (int i=0;i<5;i++) {
				//System.out.print(".");
				SV[1] = 120;
				HL[0] = 70;
				reccog.setSV(SV);
				reccog.setHL(HL);
				reccog.updateGui(reccog.BLUE, 20);
				blueLines += reccog.getLineCount();
				SV[1] = 70;
				HL[0] = 70;
				reccog.setSV(SV);
				reccog.setHL(HL);
				reccog.updateGui(reccog.RED, 30);
				redLines += reccog.getLineCount();
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			//If all else fails, at least the capture closes
			reccog.endCapture();
		}
		System.out.println();
		System.out.println("Red lines: "+redLines);
		System.out.println("Blue lines: "+blueLines);
	}
}
