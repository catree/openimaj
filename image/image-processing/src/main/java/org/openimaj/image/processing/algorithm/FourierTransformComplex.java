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
package org.openimaj.image.processing.algorithm;

import org.openimaj.image.FImage;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 * Perform forward and inverse Fast Fourier Transforms on image data. This class
 * computes the result of the transform in complex form. If you want the result
 * in polar form (in terms of phase and magnitude) then use the
 * {@link FourierTransform} instead.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FourierTransformComplex {
	private FImage real;
	private FImage imaginary;
	private boolean centre;

	/**
	 * Construct Fourier Transform by performing a forward transform on the
	 * given image. If the centre option is set, the FFT will be re-ordered so
	 * that the DC component is in the centre.
	 *
	 * @param image
	 *            the image to transform
	 * @param centre
	 *            should the FFT be reordered so the centre is DC component
	 */
	public FourierTransformComplex(FImage image, boolean centre) {
		this.centre = centre;

		process(image);
	}

	/**
	 * Construct Fourier Transform object from the given magnitude and phase
	 * images in the frequency domain. The resultant object can then be used to
	 * construct the image using the {@link #inverse()} method.
	 *
	 * @param real
	 *            the real image
	 * @param imaginary
	 *            the imaginary image
	 * @param centre
	 *            is the DC component in the image centre?
	 */
	public FourierTransformComplex(FImage real, FImage imaginary, boolean centre) {
		this.centre = centre;
		this.real = real;
		this.imaginary = imaginary;
	}

	private void process(FImage image) {
		final int cs = image.getCols();
		final int rs = image.getRows();

		real = new FImage(cs, rs);
		imaginary = new FImage(cs, rs);

		final FloatFFT_2D fft = new FloatFFT_2D(rs, cs);
		final float[][] prepared = FourierTransform.prepareData(image.pixels, rs, cs, centre);

		fft.complexForward(prepared);

		for (int y = 0; y < rs; y++) {
			for (int x = 0; x < cs; x++) {
				real.pixels[y][x] = prepared[y][x * 2];
				imaginary.pixels[y][x] = prepared[y][1 + x * 2];
			}
		}
	}

	/**
	 * Perform the inverse FFT using the underlying magnitude and phase images.
	 * The resultant reconstructed image may need normalisation.
	 *
	 * @return the reconstructed image
	 */
	public FImage inverse() {
		final int cs = real.getCols();
		final int rs = real.getRows();

		final FloatFFT_2D fft = new FloatFFT_2D(rs, cs);
		final float[][] prepared = new float[rs][cs * 2];
		for (int y = 0; y < rs; y++) {
			for (int x = 0; x < cs; x++) {
				prepared[y][x * 2] = real.pixels[y][x];
				prepared[y][1 + x * 2] = imaginary.pixels[y][x];
			}
		}

		fft.complexInverse(prepared, true);

		final FImage image = new FImage(cs, rs);
		FourierTransform.unprepareData(prepared, image, centre);

		return image;
	}

	/**
	 * @return the real image
	 */
	public FImage getReal() {
		return real;
	}

	/**
	 * @return the imaginary image
	 */
	public FImage getImaginary() {
		return imaginary;
	}

	/**
	 * @return true if the DC component is in the centre; false otherwise
	 */
	public boolean isCentre() {
		return centre;
	}
}
