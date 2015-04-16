package org.sunflow;

import java.io.PrintStream;
import java.net.URL;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Benchmark implements org.sunflow.system.BenchmarkTest, org.sunflow.system.UserInterface, org.sunflow.core.Display
{
    private int resolution;
    private boolean showOutput;
    private boolean showBenchmarkOutput;
    private boolean saveOutput;
    private int threads;
    private int[] referenceImage;
    private int[] validationImage;
    private int errorThreshold;

    public static void main(String[]  r0)
    {

        int i0, i1, i2, i4, i5;
        Benchmark r3, r15;
        if (r0.length != 0)
        {
            if ( ! (r0[0].equals("-regen")))
            {
                if (r0[0].equals("-bench"))
                {
                    i4 = 0;
                    i5 = 256;

                    if (r0.length > 1)
                    {
                        i4 = Integer.parseInt(r0[1]);
                    }

                    if (r0.length > 2)
                    {
                        i5 = Integer.parseInt(r0[2]);
                    }

                    r15 = new Benchmark(i5, false, true, false, i4);
                    r15.kernelBegin();
                    r15.kernelMain();
                    r15.kernelEnd();
                }
            }
            else
            {
                int[] $r9 = {32 , 64 , 96 , 128 , 256 , 384 , 512};

                for (i0 = $r9.length , i1 = 0; i1 < i0; i1++)
                {
                    i2 = $r9[i1];
                    r3 = new Benchmark(i2, true, false, true);
                    r3.kernelMain();
                }
            }
        }
        else
        {
            System.out.println("Benchmark options:");
            System.out.println("  -regen                        Regenerate reference images for a variety of sizes");
            System.out.println("  -bench [threads] [resolution] Run a single iteration of the benchmark using the specified thread count and image resolution");
            System.out.println("                                Default: threads=0 (auto-detect cpus), resolution=256");
        }
    }

    public Benchmark()
    {
        this(384, false, true, false);

    }

    public Benchmark(int  i0, boolean  z0, boolean  z1, boolean  z2)
    {
        this(i0, z0, z1, z2, 0);

    }

    public Benchmark(int  i0, boolean  z0, boolean  z1, boolean  z2, int  i1)
    {

        URL r1;
        BufferedImage r2;
        int i2, i3;
        UI$Module $r11;
        UI.set(this);
        resolution = i0;
        showOutput = z0;
        showBenchmarkOutput = z1;
        saveOutput = z2;
        threads = i1;
        errorThreshold = 6;

        if ( ! (z2))
        {
            Object[] $r4 = {Integer.valueOf(i0)};
            r1 = class "org/sunflow/Benchmark".getResource(String.format("/resources/golden_%04X.png", $r4));

            if (r1 == null)
            {
                UI.printError(UI$Module.BENCH, "Unable to find reference frame!", new Object[0]);
            }

            Object[] $r10 = {r1};
            UI.printInfo(UI$Module.BENCH, "Loading reference image from: %s", $r10);

            try
            {
                r2 = ImageIO.read(r1);

                label_0:
                {
                    if (r2.getWidth() == i0 && r2.getHeight() == i0)
                    {
                        break label_0;
                    }

                    $r11 = UI$Module.BENCH;
                    Object[] $r12 = {Integer.valueOf(i0) , Integer.valueOf(i0) , Integer.valueOf(r2.getWidth()) , Integer.valueOf(r2.getHeight())};
                    UI.printError($r11, "Reference image has invalid resolution! Expected %dx%d found %dx%d", $r12);
                } //end label_0:


                referenceImage = new int[i0 * i0];
                i2 = 0;
                i3 = 0;

                while (i2 < i0)
                {

                    if (0 >= i0)
                    {
                        i2++;
                    }
                    else
                    {
                        referenceImage[i3] = r2.getRGB(0, i0 - 1 - i2);
                        i3++;
                    }
                }
            }
            catch (IOException $r19)
            {
                UI.printError(UI$Module.BENCH, "Unable to load reference frame!", new Object[0]);
            }

            return;
        }
    }

    public void execute()
    {

        BenchmarkFramework r2;
        r2 = new BenchmarkFramework(10, 600);
        r2.execute(this);
    }

    public void kernelBegin()
    {


        validationImage = new int[resolution * resolution];
    }

    public void kernelMain()
    {

        Benchmark$BenchmarkScene $r1;
        $r1 = new Benchmark$BenchmarkScene(this);
    }

    public void kernelEnd()
    {

        int i0, i1, i11, i20;
        UI$Module $r11;
        i0 = 0;

        label_2:
        {
            if (referenceImage != null && validationImage.length == referenceImage.length)
            {
                for (i1 = 0; i1 < validationImage.length; i1++)
                {
                    i11 = i0 + Math.abs((validationImage[i1] & 255) - (referenceImage[i1] & 255));
                    i20 = i11 + Math.abs((validationImage[i1] >> 8 & 255) - (referenceImage[i1] >> 8 & 255));
                    i0 = i20 + Math.abs((validationImage[i1] >> 16 & 255) - (referenceImage[i1] >> 16 & 255));
                }

                if (i0 <= errorThreshold)
                {
                    UI.printInfo(UI$Module.BENCH, "Image check passed!", new Object[0]);
                    break label_2;
                }

                $r11 = UI$Module.BENCH;
                Object[] $r12 = {Integer.valueOf(i0)};
                UI.printError($r11, "Image check failed! - #errors: %d", $r12);
                break label_2;
            }

            UI.printError(UI$Module.BENCH, "Image check failed! - reference is not comparable", new Object[0]);
        } //end label_2:

    }

    public void print(UI$Module  r1, UI$PrintLevel  r2, String  r3)
    {


        label_3:
        {
            if ( ! (showOutput) &&  ( ! (showBenchmarkOutput) || r1 != UI$Module.BENCH) )
            {
                break label_3;
            }

            System.out.println(UI.formatOutput(r1, r2, r3));
        } //end label_3:


        if (r2 != UI$PrintLevel.ERROR)
        {
            return;
        }

        throw new RuntimeException(r3);
    }

    public void taskStart(String  r1, int  i0, int  i1)
    {

    }

    public void taskStop()
    {

    }

    public void taskUpdate(int  i0)
    {

    }

    public void imageBegin(int  i0, int  i1, int  i2)
    {

    }

    public void imageEnd()
    {

    }

    public void imageFill(int  i0, int  i1, int  i2, int  i3, Color  r1)
    {

    }

    public void imagePrepare(int  i0, int  i1, int  i2, int  i3, int  i4)
    {

    }

    public void imageUpdate(int  i0, int  i1, int  i2, int  i3, Color[]  r1)
    {

        int i4, i5, i7;
        i4 = 0;
        i5 = 0;

        while (i4 < i3)
        {
            i7 = i0 + resolution * (resolution - 1 - i1);

            if (0 >= i2)
            {
                i4++;
                i1++;
            }
            else
            {
                validationImage[i7] = r1[i5].copy().toNonLinear().toRGB();
                i5++;
                i7++;
            }
        }
    }

    static boolean access$000(Benchmark  r0)
    {


        return r0.saveOutput;
    }

    static int access$100(Benchmark  r0)
    {


        return r0.resolution;
    }

    static int access$200(Benchmark  r0)
    {


        return r0.threads;
    }
}
