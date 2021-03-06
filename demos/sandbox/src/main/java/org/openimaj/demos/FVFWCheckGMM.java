/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.demos;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.openimaj.feature.FloatFV;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.dense.gradient.dsift.FloatDSIFTKeypoint;
import org.openimaj.image.feature.local.aggregate.FisherVector;
import org.openimaj.math.statistics.distribution.DiagonalMultivariateGaussian;
import org.openimaj.math.statistics.distribution.MixtureOfGaussians;
import org.openimaj.math.statistics.distribution.MultivariateGaussian;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLSingle;
import com.jmatio.types.MLStructure;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class FVFWCheckGMM {

	private static final String GMM_MATLAB_FILE = "/Users/ss/Experiments/FVFW/data/gmm_512.mat";
	private static final String[] FACE_DSIFTS_PCA = new String[] {
			"/Users/ss/Experiments/FVFW/data/aaron-pcadsiftaug.mat"
	};

	public static void main(String[] args) throws IOException {
		final MixtureOfGaussians mog = loadMoG();
		final FisherVector<float[]> fisher = new FisherVector<float[]>(mog, true, true);
		for (final String faceFile : FACE_DSIFTS_PCA) {
			final MemoryLocalFeatureList<FloatDSIFTKeypoint> loadDSIFTPCA = loadDSIFTPCA(faceFile);

			final FloatFV fvec = fisher.aggregate(loadDSIFTPCA);
			System.out.println(String.format("%s: %s", faceFile, fvec));
			System.out.println("Writing...");

			final File out = new File(faceFile + ".fisher.mat");
			final MLArray data = toMLArray(fvec);
			new MatFileWriter(out, Arrays.asList(data));
		}
	}

	private static MemoryLocalFeatureList<FloatDSIFTKeypoint> loadDSIFTPCA(String faceFile) throws IOException {
		final File f = new File(faceFile);
		final MatFileReader reader = new MatFileReader(f);
		final MLSingle feats = (MLSingle) reader.getContent().get("feats");
		final int nfeats = feats.getN();
		final MemoryLocalFeatureList<FloatDSIFTKeypoint> ret = new MemoryLocalFeatureList<FloatDSIFTKeypoint>();
		for (int i = 0; i < nfeats; i++) {
			final FloatDSIFTKeypoint feature = new FloatDSIFTKeypoint();
			feature.descriptor = new float[feats.getM()];
			for (int j = 0; j < feature.descriptor.length; j++) {
				feature.descriptor[j] = feats.get(j, i);
			}
			ret.add(feature);
		}

		return ret;
	}

	private static MLArray toMLArray(FloatFV fvec) {
		final MLDouble data = new MLDouble("fisherface", new int[] { fvec.values.length, 1 });
		for (int i = 0; i < fvec.values.length; i++) {
			data.set((double) fvec.values[i], i, 0);
		}
		return data;
	}

	private static MixtureOfGaussians loadMoG() throws IOException {
		final File f = new File(GMM_MATLAB_FILE);
		final MatFileReader reader = new MatFileReader(f);
		final MLStructure codebook = (MLStructure) reader.getContent().get("codebook");

		final MLSingle mean = (MLSingle) codebook.getField("mean");
		final MLSingle variance = (MLSingle) codebook.getField("variance");
		final MLSingle coef = (MLSingle) codebook.getField("coef");

		final int n_gaussians = mean.getN();
		final int n_dims = mean.getM();

		final MultivariateGaussian[] ret = new MultivariateGaussian[n_gaussians];
		final double[] weights = new double[n_gaussians];
		for (int i = 0; i < n_gaussians; i++) {
			weights[i] = coef.get(i, 0);
			final DiagonalMultivariateGaussian d = new DiagonalMultivariateGaussian(n_dims);
			for (int j = 0; j < n_dims; j++) {
				d.mean.set(0, j, mean.get(j, i));
				d.variance[j] = variance.get(j, i);
			}
			ret[i] = d;
		}

		return new MixtureOfGaussians(ret, weights);
	}

}
