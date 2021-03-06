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
package org.openimaj.math.geometry.shape.util;

import org.junit.Assert;
import org.junit.Test;
import org.openimaj.math.geometry.shape.RotatedRectangle;

/**
 * Tests for rotating calipers
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class RotatingCalipersTest {
	/**
	 * Test wide rect rotation
	 */
	@Test
	public void testWide() {
		final RotatedRectangle rr = new RotatedRectangle(0, 0, 50, 100, 0);

		for (float angle = 0; angle < Math.PI / 2; angle += 0.01) {
			rr.rotation = angle;

			final RotatedRectangle rr2 = rr.asPolygon().minimumBoundingRectangle();

			Assert.assertEquals(rr.rotation, rr2.rotation, 0.1);
			Assert.assertEquals(rr.height, rr2.height, 0.1);
			Assert.assertEquals(rr.width, rr2.width, 0.1);
		}
	}

	/**
	 * Test tall rect rotation
	 */
	@Test
	public void testTall() {
		final RotatedRectangle rr = new RotatedRectangle(0, 0, 100, 50, 0);

		for (float angle = 0; angle < Math.PI / 2; angle += 0.01) {
			rr.rotation = angle;

			final RotatedRectangle rr2 = rr.asPolygon().minimumBoundingRectangle();

			Assert.assertEquals(rr.rotation, rr2.rotation, 0.1);
			Assert.assertEquals(rr.height, rr2.height, 0.1);
			Assert.assertEquals(rr.width, rr2.width, 0.1);
		}
	}

	/**
	 * Test square rotation
	 */
	@Test
	public void testSquare() {
		final RotatedRectangle rr = new RotatedRectangle(0, 0, 100, 100, 0);

		for (float angle = 0; angle < Math.PI / 2; angle += 0.01) {
			rr.rotation = angle;

			final RotatedRectangle rr2 = rr.asPolygon().minimumBoundingRectangle();

			Assert.assertEquals(rr.rotation, rr2.rotation, 0.1);
			Assert.assertEquals(rr.height, rr2.height, 0.1);
			Assert.assertEquals(rr.width, rr2.width, 0.1);
		}
	}
}
