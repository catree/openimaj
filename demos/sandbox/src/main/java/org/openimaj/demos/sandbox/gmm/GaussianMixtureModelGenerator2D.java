package org.openimaj.demos.sandbox.gmm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.math.statistics.distribution.MultivariateGaussian;

import Jama.CholeskyDecomposition;
import Jama.Matrix;

public class GaussianMixtureModelGenerator2D implements GaussianMixtureModelGenerator{



	private static final int N_POINTS = 200;
	private List<MultivariateGaussian> normList;
	private Random random;
	private double[] pi;
	private GaussianMixtureModelGenerator2D() {
		normList = new ArrayList<MultivariateGaussian>();
		this.random = new Random();
	}
	/**
	 * @param ellipses
	 */
	public GaussianMixtureModelGenerator2D(Ellipse ... ellipses) {
		this();
		for (Ellipse ellipse : ellipses) {
			Matrix mean = new Matrix(1,2);
			Point2d cog = ellipse.getCOG();
			Ellipse corrected = new Ellipse(cog.getX(), cog.getY(), ellipse.getMajor()/2, ellipse.getMinor()/2, ellipse.getRotation());
			Matrix covar = EllipseUtilities.ellipseToCovariance(corrected);
			mean.set(0, 0, cog.getX());
			mean.set(0, 1, cog.getY());

			normList.add(new MultivariateGaussian(mean, covar));
		}

		this.pi = new double[normList.size()];
		for (int i = 0; i < pi.length; i++) {
			pi[i] = 1f/pi.length;
		}
	}

	@Override
	public Generated generate() {
		Generated g = new Generated();
		double probZ = random.nextDouble();
		double sum = 0;
		g.distribution = this.pi.length - 1;
		for (int i = 0; i < this.pi.length; i++) {
			sum += pi[i];
			if(sum > probZ){
				g.distribution = i;
				break;
			}
		}

		MultivariateGaussian distrib = this.normList.get(g.distribution);
		Matrix mean = distrib.getMean().transpose();
		Matrix covar = distrib.getCovar();
		CholeskyDecomposition decomp = new CholeskyDecomposition(covar);
		Matrix r = MatrixUtils.randGaussian(2,1);

		Matrix genPoint = mean.plus(decomp.getL().times(r));
		g.point = new double[]{genPoint.get(0, 0),genPoint.get(1,0)};
		g.responsibilities = new double[this.pi.length];

		sum = 0;
		for (int i = 0; i < g.responsibilities.length; i++) {
			sum += g.responsibilities[i] = this.normList.get(i).estimateProbability(new float[]{(float) (g.point[0]),(float) g.point[1]});
		}

		for (int i = 0; i < g.responsibilities.length; i++) {
			g.responsibilities[i] /= sum;
		}

		return g;
	}

	public static void main(String[] args) {
		Ellipse e1 = new Ellipse(200, 200, 40, 20, Math.PI/3);
		Ellipse e2 = new Ellipse(220, 150, 60, 20, -Math.PI/3);
		Ellipse e3 = new Ellipse(180, 200, 80, 20, -Math.PI/3);
		Float[][] colours = new Float[][]{RGBColour.RED,RGBColour.GREEN,RGBColour.BLUE};
		MBFImage image = new MBFImage(400,400,3);
		image.drawShape(e1, RGBColour.RED);
		image.drawShape(e2, RGBColour.GREEN);
		image.drawShape(e3, RGBColour.BLUE);

		GaussianMixtureModelGenerator2D gmm = new GaussianMixtureModelGenerator2D(e1,e2,e3);
		MBFImage imageUnblended = image.clone();
		MBFImage imageBlended = image.clone();
		for (int i = 0; i < N_POINTS; i++) {
			Generated gen = gmm.generate();
			Point2d p = new Point2dImpl((float)gen.point[0],(float) gen.point[1]);
			imageUnblended.drawPoint(p, colours[gen.distribution], 3);
			Float[] weightedColour = new Float[3];
			for (int j = 0; j < weightedColour.length; j++) {
				weightedColour[j] = 0f;
			}
			for (int colour = 0; colour < colours.length; colour++) {
				for (int channel = 0; channel < colours[colour].length; channel++) {
					weightedColour[channel] = (float) (weightedColour[channel] + colours[colour][channel] * gen.responsibilities[colour]);
				}
			}
			double sumWeight = 0;
			for (int j = 0; j < weightedColour.length; j++) {
				sumWeight += weightedColour[j];
			}
			for (int j = 0; j < weightedColour.length; j++) {
				weightedColour[j] = (float) (weightedColour[j] / sumWeight);
			}
			imageBlended.drawPoint(p, weightedColour, 3);
		}

		DisplayUtilities.display(imageUnblended);
		DisplayUtilities.display(imageBlended);

	}
	@Override
	public int dimentions() {
		return 2;
	}

}
