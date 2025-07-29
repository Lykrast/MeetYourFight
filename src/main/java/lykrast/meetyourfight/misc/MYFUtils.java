package lykrast.meetyourfight.misc;

import net.minecraft.util.Mth;

public class MYFUtils {
	//ripped that back from bpas

	//those take a float from 0 to 1, for animation smoothing
	public static float easeInQuad(float progress) {
		return progress * progress;
	}

	public static float easeOutQuad(float progress) {
		progress = 1 - progress;
		return 1 - (progress * progress);
	}

	public static float easeInOut(float progress) {
		//https://math.stackexchange.com/questions/121720/ease-in-out-function/121755#121755
		//TODO hey maybe smoothstep is better? 3x^2-2x^3, less jagged and less maths but like dunno if I could spot the differences, will have to test when I use that
		float sq = progress * progress;
		return sq / (2 * (sq - progress) + 1);
	}
	
	public static float easeInQuart(float progress) {
		progress *= progress;
		return progress * progress;
	}

	public static float easeOutQuart(float progress) {
		progress = 1 - progress;
		progress *= progress;
		return 1 - (progress * progress);
	}

	//this is protected in humanoidmodel
	public static float rotlerpRad(float progress, float start, float end) {
		float f = (end - start) % Mth.TWO_PI;
		if (f < -Mth.PI) {
			f += Mth.TWO_PI;
		}

		if (f >= Mth.PI) {
			f -= Mth.TWO_PI;
		}

		return start + progress * f;
	}

}
