BEGIN{for(int i = 0; i < x; i++) {
		x--
	}
}
(a = 3){ for(int i = 0; i < x; i++) {
		x--
	}
}
END{
for(int i = 0; i < x; i++) {
		x--
	}
}